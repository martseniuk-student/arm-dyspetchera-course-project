package database;

import model.Stop;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicketDAO {

    // SQL Запити (Константи)
    private static final String SELECT_ALL_TICKETS_DATA =
            "SELECT t.id, r.number AS routeNumber, se.departureTime, st.name AS stopName, o.paymentType, t.price " +
            "FROM ticket t " +
            "JOIN `order` o ON t.orderId = o.id " +
            "JOIN scheduleentry se ON t.entryId = se.id " +
            "JOIN route r ON se.routeId = r.id " +
            "JOIN stop st ON t.stopId = st.id " +
            "ORDER BY t.id DESC";

    private static final String SELECT_STOPS_BY_ROUTE =
            "SELECT s.id, s.name FROM stop s " +
            "JOIN routestops rs ON s.id = rs.stopId " +
            "WHERE rs.routeId = ?";

    private static final String INSERT_ORDER =
            "INSERT INTO `order` (ticketCount, totalPrice, paymentType) VALUES (?, ?, ?)";

    private static final String INSERT_PAYMENT =
            "INSERT INTO payment (amount, orderId) VALUES (?, ?)";

    private static final String INSERT_TICKET =
            "INSERT INTO ticket (price, orderId, entryId, stopId) VALUES (?, ?, ?, ?)";


    // ==========================================
    // CREATE-операції
    // ==========================================

    // Комплексна генерація вказаної кількості квитків (транзакційно)
    // Створює замовлення, платіж та пакет квитків
    public boolean generateTickets(int entryId, int routeId, int count, int stopId, String paymentType, double singlePrice) {
        List<Stop> routeStops = null;

        // Якщо обрано випадкові зупинки
        if (stopId == -1) {
            routeStops = getStopsByRoute(routeId);
            // Якщо для маршруту не додано жодної зупинки, ми не можемо згенерувати випадкову
            if (routeStops.isEmpty()) {
                System.err.println("Неможливо згенерувати квитки: у маршруту " + routeId + " відсутні зупинки.");
                return false;
            }
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Початок транзакції
            int orderId = -1;

            // 1. Створюємо замовлення
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_ORDER, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, count);
                pstmt.setDouble(2, count * singlePrice);
                pstmt.setString(3, paymentType);
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    }
                }
            }

            if (orderId == -1) {
                conn.rollback();
                return false;
            }

            // 2. Створюємо оплату
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_PAYMENT)) {
                pstmt.setDouble(1, count * singlePrice);
                pstmt.setInt(2, orderId);
                pstmt.executeUpdate();
            }

            // 3. Генеруємо квитки за допомогою Batch-пакета
            Random random = new Random();
            try (PreparedStatement pstmt = conn.prepareStatement(INSERT_TICKET)) {
                for (int i = 0; i < count; i++) {
                    int currentStopId = stopId;
                    if (stopId == -1 && routeStops != null) {
                        currentStopId = routeStops.get(random.nextInt(routeStops.size())).getId();
                    }

                    pstmt.setDouble(1, singlePrice);
                    pstmt.setInt(2, orderId);
                    pstmt.setInt(3, entryId);
                    pstmt.setInt(4, currentStopId);
                    pstmt.addBatch(); // Додаємо в пакет
                }
                pstmt.executeBatch(); // Виконуємо всі INSERT за один раз
            }

            conn.commit(); // Зберігаємо транзакцію
            return true;

        } catch (SQLException e) {
            System.err.println("Помилка під час генерації квитків. Робимо відкат транзакції: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Не вдалося зробити відкат транзакції: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Не вдалося закрити з'єднання: " + e.getMessage());
                }
            }
        }
    }

    // ==========================================
    // READ-операції
    // ==========================================

    // Отримує дані для таблиці "Історія квитків"
    public List<Object[]> getAllTicketsData() {
        List<Object[]> data = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_TICKETS_DATA)) {

            while (rs.next()) {
                String time = rs.getTime("departureTime") != null
                        ? rs.getTime("departureTime").toString().substring(0, 5)
                        : "—";

                data.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("routeNumber"),
                        time,
                        rs.getString("stopName"),
                        rs.getString("paymentType"),
                        rs.getDouble("price")
                });
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні історії квитків: " + e.getMessage());
        }
        return data;
    }

    // Отримує зупинки тільки для конкретного маршруту
    public List<Stop> getStopsByRoute(int routeId) {
        List<Stop> stops = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_STOPS_BY_ROUTE)) {

            pstmt.setInt(1, routeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stops.add(new Stop(
                            rs.getInt("id"),
                            rs.getString("name")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні зупинок для маршруту (ID: " + routeId + "): " + e.getMessage());
        }
        return stops;
    }
}