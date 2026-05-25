package database;

import model.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDAO {

    // SQL Запити (Константи)
    private static final String SELECT_SCHEDULE_BY_DATE =
            "SELECT se.id, se.departureTime, " +
                    "r.id AS routeId, r.number AS routeNumber, " +
                    "t.id AS transportId, t.boardNumber, t.status AS transportStatus, " +
                    "tt.id AS typeId, tt.name AS typeName, tt.capacity, " +
                    "s.id AS scheduleId, s.date AS scheduleDate, s.status AS scheduleStatus " +
                    "FROM scheduleentry se " +
                    "JOIN route r ON se.routeId = r.id " +
                    "JOIN transport t ON se.transportId = t.id " +
                    "JOIN transporttype tt ON t.typeId = tt.id " +
                    "JOIN `schedule` s ON se.scheduleId = s.id " +
                    "WHERE s.date = ? ORDER BY se.departureTime";

    private static final String SELECT_ASSIGNMENTS_FOR_ENTRY =
            "SELECT pa.id, p.id AS personnelId, p.fullName, p.status, r.id AS roleId, r.name AS roleName " +
                    "FROM personnelassignment pa " +
                    "JOIN personnel p ON pa.personnelId = p.id " +
                    "JOIN role r ON p.roleId = r.id " +
                    "WHERE pa.entryId = ?";

    private static final String INSERT_SCHEDULE_ENTRY =
            "INSERT INTO scheduleentry (routeId, transportId, scheduleId, departureTime) VALUES (?, ?, ?, ?)";

    private static final String INSERT_PERSONNEL_ASSIGNMENT =
            "INSERT INTO personnelassignment (personnelId, entryId) VALUES (?, ?)";

    private static final String UPDATE_SCHEDULE_ENTRY =
            "UPDATE scheduleentry SET routeId = ?, transportId = ?, departureTime = ? WHERE id = ?";

    private static final String DELETE_PERSONNEL_ASSIGNMENTS =
            "DELETE FROM personnelassignment WHERE entryId = ?";

    private static final String DELETE_SCHEDULE_ENTRY =
            "DELETE FROM scheduleentry WHERE id = ?";

    private static final String UPDATE_SCHEDULE_STATUS =
            "UPDATE `schedule` SET status = 'Active' WHERE date = ?";

    private static final String SELECT_SCHEDULE_ID =
            "SELECT id FROM `schedule` WHERE date = ?";

    private static final String INSERT_SCHEDULE =
            "INSERT INTO `schedule` (date, status) VALUES (?, 'Draft')";

    // ==========================================
    // CREATE-операції
    // ==========================================

    // Додає новий запис до розкладу з призначенням списку персоналу (транзакційно)
    public boolean addSchedule(LocalDate date, int routeId, int transportId, List<Integer> personnelIds, String departureTime) {
        int scheduleId = getOrCreateScheduleId(date);
        if (scheduleId == -1) return false;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int newEntryId = -1;

            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_SCHEDULE_ENTRY, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, routeId);
                pstmt.setInt(2, transportId);
                pstmt.setInt(3, scheduleId);

                String timeStr = departureTime.length() == 5 ? departureTime + ":00" : departureTime;
                pstmt.setTime(4, Time.valueOf(LocalTime.parse(timeStr)));

                pstmt.executeUpdate();
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) newEntryId = rs.getInt(1);
                }
            }

            if (newEntryId != -1 && personnelIds != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(INSERT_PERSONNEL_ASSIGNMENT)) {
                    for (int pId : personnelIds) {
                        if (pId > 0) {
                            pstmt.setInt(1, pId);
                            pstmt.setInt(2, newEntryId);
                            pstmt.addBatch();
                        }
                    }
                    pstmt.executeBatch();
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            System.err.println("Помилка при додаванні запису розкладу. Робимо відкат: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("Відкат не вдався: " + ex.getMessage()); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { System.err.println("Не вдалося закрити з'єднання: " + e.getMessage()); }
            }
        }
    }

    // ==========================================
    // READ-операції
    // ==========================================

    // Отримує розклад на певну дату разом з призначеннями персоналу
    public List<ScheduleEntry> getSchedulesByDate(LocalDate date) {
        List<ScheduleEntry> entries = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_SCHEDULE_BY_DATE)) {

            pstmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Route route = new Route(rs.getInt("routeId"), rs.getString("routeNumber"));

                    TransportType tType = new TransportType(rs.getInt("typeId"), rs.getString("typeName"), rs.getInt("capacity"));
                    Transport transport = new Transport(rs.getInt("transportId"), rs.getString("boardNumber"), rs.getString("transportStatus"), tType);

                    Schedule schedule = new Schedule(rs.getInt("scheduleId"), rs.getDate("scheduleDate").toLocalDate(), rs.getString("scheduleStatus"));

                    LocalTime depTime = rs.getTime("departureTime") != null ? rs.getTime("departureTime").toLocalTime() : null;
                    int entryId = rs.getInt("id");

                    List<PersonnelAssignment> assignments = getAssignmentsForEntry(entryId);
                    entries.add(new ScheduleEntry(entryId, depTime, route, transport, schedule, assignments));
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні розкладу за дату: " + e.getMessage());
        }
        return entries;
    }

    // Допоміжний метод для отримання призначеного персоналу на рейс
    private List<PersonnelAssignment> getAssignmentsForEntry(int entryId) {
        List<PersonnelAssignment> assignments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ASSIGNMENTS_FOR_ENTRY)) {

            pstmt.setInt(1, entryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Role role = new Role(rs.getInt("roleId"), rs.getString("roleName"));
                    Personnel person = new Personnel(rs.getInt("personnelId"), rs.getString("fullName"), rs.getString("status"), role);

                    PersonnelAssignment assignment = new PersonnelAssignment();
                    assignment.setPersonnel(person);
                    assignments.add(assignment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні призначень персоналу для запису " + entryId + ": " + e.getMessage());
        }
        return assignments;
    }

    // ==========================================
    // UPDATE-операції
    // ==========================================

    // Оновлює інформацію про рейс та перезаписує список персоналу (транзакційно)
    public boolean updateSchedule(int entryId, int routeId, int transportId, List<Integer> personnelIds, String departureTime) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_SCHEDULE_ENTRY)) {
                pstmt.setInt(1, routeId);
                pstmt.setInt(2, transportId);

                String timeStr = departureTime.length() == 5 ? departureTime + ":00" : departureTime;
                pstmt.setTime(3, Time.valueOf(LocalTime.parse(timeStr)));
                pstmt.setInt(4, entryId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(DELETE_PERSONNEL_ASSIGNMENTS)) {
                pstmt.setInt(1, entryId);
                pstmt.executeUpdate();
            }

            if (personnelIds != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(INSERT_PERSONNEL_ASSIGNMENT)) {
                    for (int pId : personnelIds) {
                        if (pId > 0) {
                            pstmt.setInt(1, pId);
                            pstmt.setInt(2, entryId);
                            pstmt.addBatch();
                        }
                    }
                    pstmt.executeBatch();
                }
            }
            conn.commit();
            return true;
        } catch (Exception e) {
            System.err.println("Помилка при оновленні запису розкладу. Робимо відкат: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("Відкат не вдався: " + ex.getMessage()); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { System.err.println("Не вдалося закрити з'єднання: " + e.getMessage()); }
            }
        }
    }

    // Затверджує розклад на дату (змінює статус на 'Active')
    public boolean approveScheduleForDate(LocalDate date) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_SCHEDULE_STATUS)) {

            pstmt.setDate(1, Date.valueOf(date));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при затвердженні розкладу: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // DELETE-операції
    // ==========================================

    // Видаляє рейс та всі зв'язані з ним призначення персоналу (транзакційно)
    public boolean deleteSchedule(int entryId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(DELETE_PERSONNEL_ASSIGNMENTS)) {
                pstmt.setInt(1, entryId);
                pstmt.executeUpdate();
            }

            boolean success;
            try (PreparedStatement pstmt = conn.prepareStatement(DELETE_SCHEDULE_ENTRY)) {
                pstmt.setInt(1, entryId);
                success = pstmt.executeUpdate() > 0;
            }

            conn.commit();
            return success;
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні запису розкладу. Робимо відкат: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("Відкат не вдався: " + ex.getMessage()); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { System.err.println("Не вдалося закрити з'єднання: " + e.getMessage()); }
            }
        }
    }

    // ==========================================
    // Службові приватні методи
    // ==========================================

    // Повертає ID наявного розкладу або створює новий зі статусом 'Draft'
    private int getOrCreateScheduleId(LocalDate date) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(SELECT_SCHEDULE_ID)) {

            selectStmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(INSERT_SCHEDULE, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setDate(1, Date.valueOf(date));
                insertStmt.executeUpdate();
                try (ResultSet rs = insertStmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка в методі getOrCreateScheduleId: " + e.getMessage());
        }
        return -1;
    }
}