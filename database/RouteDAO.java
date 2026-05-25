package database;

import model.Route;
import model.Stop;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteDAO {

    // SQL Запити (Константи)
    private static final String INSERT_ROUTE = "INSERT INTO route (number) VALUES (?)";
    private static final String INSERT_STOP = "INSERT INTO stop (name) VALUES (?)";

    private static final String SELECT_ALL_ROUTES = "SELECT * FROM route";
    private static final String SELECT_ALL_STOPS = "SELECT * FROM stop";
    private static final String SELECT_STOPS_BY_ROUTE =
            "SELECT s.id, s.name FROM stop s " +
            "JOIN routestops rs ON s.id = rs.stopId " +
            "WHERE rs.routeId = ? ORDER BY rs.stopOrder";

    private static final String UPDATE_ROUTE = "UPDATE route SET number = ? WHERE id = ?";
    private static final String UPDATE_STOP = "UPDATE stop SET name = ? WHERE id = ?";

    private static final String DELETE_ROUTE_STOPS = "DELETE FROM routestops WHERE routeId = ?";
    private static final String INSERT_ROUTE_STOP = "INSERT INTO routestops (routeId, stopId, stopOrder) VALUES (?, ?, ?)";

    private static final String DELETE_ROUTE = "DELETE FROM route WHERE id = ?";
    private static final String DELETE_STOP = "DELETE FROM stop WHERE id = ?";

    // ==========================================
    // CREATE-операції
    // ==========================================

    // Додає новий маршрут
    public boolean addRoute(String number) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_ROUTE)) {

            pstmt.setString(1, number);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при додаванні маршруту: " + e.getMessage());
            return false;
        }
    }

    // Додає нову зупинку
    public boolean addStop(String name) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_STOP)) {

            pstmt.setString(1, name);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при додаванні зупинки: " + e.getMessage());
            return false;
        }
    }

    // Додає новий маршрут і одразу повертає його ідентифікатор
    public int addRouteAndGetId(String number) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_ROUTE, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, number);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при додаванні маршруту та отриманні ID: " + e.getMessage());
        }
        return -1;
    }

    // ==========================================
    // READ-операції
    // ==========================================

    // Витягує всі маршрути
    public List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_ROUTES)) {

            while (rs.next()) {
                routes.add(new Route(
                        rs.getInt("id"),
                        rs.getString("number")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні списку маршрутів: " + e.getMessage());
        }

        return routes;
    }

    // Витягує усі зупинки
    public List<Stop> getAllStops() {
        List<Stop> stops = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_STOPS)) {

            while (rs.next()) {
                stops.add(new Stop(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні списку зупинок: " + e.getMessage());
        }
        return stops;
    }

    // Отримує всі зупинки для конкретного маршруту
    public List<Stop> getStopsByRouteId(int routeId) {
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
            System.err.println("Помилка при отриманні зупинок маршруту (ID: " + routeId + "): " + e.getMessage());
        }
        return stops;
    }

    // ==========================================
    // UPDATE-операції
    // ==========================================

    // Оновлює дані маршруту
    public boolean updateRoute(int id, String number) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_ROUTE)) {

            pstmt.setString(1, number);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні маршруту: " + e.getMessage());
            return false;
        }
    }

    // Оновлює дані зупинки
    public boolean updateStop(int id, String name) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_STOP)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні зупинки: " + e.getMessage());
            return false;
        }
    }

    // Оновлює перелік зупинок для маршруту (використовує транзакцію)
    public boolean updateRouteStops(int routeId, List<Stop> stops) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(DELETE_ROUTE_STOPS)) {
                deleteStmt.setInt(1, routeId);
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(INSERT_ROUTE_STOP)) {
                for (int i = 0; i < stops.size(); i++) {
                    insertStmt.setInt(1, routeId);
                    insertStmt.setInt(2, stops.get(i).getId());
                    insertStmt.setInt(3, i + 1);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні зупинок маршруту. Робимо відкат: " + e.getMessage());
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
    // DELETE-операції
    // ==========================================

    // Видаляє маршрут
    public boolean deleteRoute(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_ROUTE)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні маршруту (можливо, до нього прив'язані рейси): " + e.getMessage());
            return false;
        }
    }

    // Видаляє зупинку
    public boolean deleteStop(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_STOP)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні зупинки (можливо, вона використовується в маршрутах): " + e.getMessage());
            return false;
        }
    }
}