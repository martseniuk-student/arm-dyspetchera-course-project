package database;

import model.Transport;
import model.TransportType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransportDAO {

    // SQL Запити (Константи)
    private static final String INSERT_TRANSPORT =
            "INSERT INTO transport (boardNumber, status, typeId) VALUES (?, ?, ?)";

    private static final String INSERT_TRANSPORT_TYPE =
            "INSERT INTO transporttype (name, capacity) VALUES (?, ?)";

    private static final String SELECT_ALL_TRANSPORT =
            "SELECT t.*, tt.name as TypeName, tt.capacity " +
            "FROM transport t " +
            "JOIN transporttype tt ON t.typeId = tt.id";

    private static final String SELECT_ALL_TRANSPORT_TYPES =
            "SELECT * FROM transporttype";

    private static final String UPDATE_TRANSPORT =
            "UPDATE transport SET boardNumber = ?, status = ?, typeId = ? WHERE id = ?";

    private static final String UPDATE_TRANSPORT_TYPE =
            "UPDATE transporttype SET name = ?, capacity = ? WHERE id = ?";

    private static final String DELETE_TRANSPORT =
            "DELETE FROM transport WHERE id = ?";

    private static final String DELETE_TRANSPORT_TYPE =
            "DELETE FROM transporttype WHERE id = ?";

    // ==========================================
    // CREATE-операції
    // ==========================================

    // Додає новий транспортний засіб
    public boolean addTransport(String boardNumber, String status, int typeId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_TRANSPORT)) {

            pstmt.setString(1, boardNumber);
            pstmt.setString(2, status);
            pstmt.setInt(3, typeId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при додаванні транспорту: " + e.getMessage());
            return false;
        }
    }

    // Додає новий тип (модель) транспорту і повертає його ID
    public int addTransportTypeAndGetId(String name, int capacity) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_TRANSPORT_TYPE, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, capacity);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при додаванні типу транспорту: " + e.getMessage());
        }
        return -1;
    }

    // ==========================================
    // READ-операції
    // ==========================================

    // Витягує список усього транспорту разом з інформацією про модель
    public List<Transport> getAllTransport() {
        List<Transport> transports = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_TRANSPORT)) {

            while (rs.next()) {
                TransportType type = new TransportType(
                        rs.getInt("typeId"),
                        rs.getString("TypeName"),
                        rs.getInt("capacity")
                );

                Transport transport = new Transport(
                        rs.getInt("id"),
                        rs.getString("boardNumber"),
                        rs.getString("status"),
                        type
                );

                transports.add(transport);
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні списку транспорту: " + e.getMessage());
        }

        return transports;
    }

    // Витягує всі наявні типи (моделі) транспорту
    public List<TransportType> getAllTransportTypes() {
        List<TransportType> types = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_TRANSPORT_TYPES)) {

            while (rs.next()) {
                types.add(new TransportType(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("capacity")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні списку типів транспорту: " + e.getMessage());
        }

        return types;
    }

    // ==========================================
    // UPDATE-операції
    // ==========================================

    // Оновлює дані наявного транспортного засобу
    public boolean updateTransport(int id, String boardNumber, String status, int typeId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_TRANSPORT)) {

            pstmt.setString(1, boardNumber);
            pstmt.setString(2, status);
            pstmt.setInt(3, typeId);
            pstmt.setInt(4, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні транспорту: " + e.getMessage());
            return false;
        }
    }

    // Оновлює назву та місткість моделі транспорту
    public boolean updateTransportType(int id, String name, int capacity) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_TRANSPORT_TYPE)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, capacity);
            pstmt.setInt(3, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні типу транспорту: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // DELETE-операції
    // ==========================================

    // Видаляє транспортний засіб за його ID
    public boolean deleteTransport(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_TRANSPORT)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні транспорту: " + e.getMessage());
            return false;
        }
    }

    // Видаляє модель транспорту за її ID
    public boolean deleteTransportType(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_TRANSPORT_TYPE)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні типу транспорту (можливо, існують автомобілі цієї моделі): " + e.getMessage());
            return false;
        }
    }
}