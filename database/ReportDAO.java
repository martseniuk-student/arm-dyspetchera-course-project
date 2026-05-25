package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // SQL Запити (Константи)
    private static final String SELECT_ROUTE_REVENUE =
            "SELECT r.number AS routeNum, COUNT(t.id) AS tCount, SUM(t.price) AS totalRev " +
            "FROM route r " +
            "JOIN scheduleentry se ON r.id = se.routeId " +
            "JOIN ticket t ON se.id = t.entryId " +
            "GROUP BY r.number " +
            "HAVING SUM(t.price) > 0 " +
            "ORDER BY SUM(t.price) DESC";

    private static final String SELECT_STOP_POPULARITY =
            "SELECT st.name AS stopName, COUNT(tk.id) AS passengerCount " +
            "FROM stop st " +
            "LEFT JOIN ticket tk ON st.id = tk.stopId " +
            "GROUP BY st.id, st.name " +
            "ORDER BY COUNT(tk.id) DESC";

    private static final String SELECT_TRANSPORT_IN_MAINTENANCE =
            "SELECT t.boardNumber, tt.name AS modelName, t.status " +
            "FROM transport t " +
            "JOIN transporttype tt ON t.typeId = tt.id " +
            "WHERE t.status = 'Under Maintenance' OR t.status = 'У ремонті'";

    private static final String SELECT_PAYMENT_TYPE_STATS =
            "SELECT paymentType, COUNT(id) AS txCount, SUM(totalPrice) AS totalSum " +
            "FROM `order` " +
            "GROUP BY paymentType";

    private static final String SELECT_TOTAL_ACTIVE_CAPACITY =
            "SELECT SUM(tt.capacity) AS totalCap " +
            "FROM transport t " +
            "JOIN transporttype tt ON t.typeId = tt.id " +
            "WHERE t.status = 'Functional' OR t.status = 'Активний'";

    private static final String SELECT_ROUTE_STOP_COUNT =
            "SELECT r.number AS routeNum, COUNT(rs.stopId) AS stopsCount " +
            "FROM route r " +
            "JOIN routestops rs ON r.id = rs.routeId " +
            "GROUP BY r.id, r.number " +
            "ORDER BY COUNT(rs.stopId) DESC";

    // ==========================================
    // READ-операції
    // ==========================================

    // Аналіз виручки та пасажиропотоку (Запит 6.3.2)
    public List<Object[]> getRouteRevenueReport() {
        List<Object[]> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ROUTE_REVENUE)) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("routeNum"),
                        rs.getInt("tCount"),
                        rs.getDouble("totalRev")
                });
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні звіту виручки маршрутів: " + e.getMessage());
        }
        return list;
    }

    // Статистика популярності зупинок (Запит 6.3.4)
    public List<Object[]> getStopPopularityReport() {
        List<Object[]> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_STOP_POPULARITY)) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("stopName"),
                        rs.getInt("passengerCount")
                });
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні звіту популярності зупинок: " + e.getMessage());
        }
        return list;
    }

    // Транспортні засоби в ремонті (Запит 6.3.5)
    public List<Object[]> getTransportInMaintenanceReport() {
        List<Object[]> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_TRANSPORT_IN_MAINTENANCE)) {

            while (rs.next()) {
                String statusDb = rs.getString("status");
                String statusUkr = "Under Maintenance".equals(statusDb) ? "У ремонті" : statusDb;

                list.add(new Object[]{
                        rs.getString("boardNumber"),
                        rs.getString("modelName"),
                        statusUkr
                });
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні звіту транспорту в ремонті: " + e.getMessage());
        }
        return list;
    }

    // Статистика за типами оплати (Запит 6.3.7)
    public List<Object[]> getPaymentTypeReport() {
        List<Object[]> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_PAYMENT_TYPE_STATS)) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("paymentType"),
                        rs.getInt("txCount"),
                        rs.getDouble("totalSum")
                });
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні звіту за типами оплати: " + e.getMessage());
        }
        return list;
    }

    // Загальна місткість всього доступного транспорту (Запит 6.3.8)
    public int getTotalActiveCapacity() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_TOTAL_ACTIVE_CAPACITY)) {

            if (rs.next()) {
                return rs.getInt("totalCap");
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні загальної місткості транспорту: " + e.getMessage());
        }
        return 0;
    }

    // Маршрути з найбільшою кількістю зупинок (Запит 6.3.9)
    public List<Object[]> getRouteStopCountReport() {
        List<Object[]> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ROUTE_STOP_COUNT)) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("routeNum"),
                        rs.getInt("stopsCount")
                });
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні звіту кількості зупинок на маршрутах: " + e.getMessage());
        }
        return list;
    }
}