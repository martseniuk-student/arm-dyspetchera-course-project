package database;

import model.Personnel;
import model.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonnelDAO {

    // SQL Запити (Константи)
    private static final String INSERT_PERSONNEL = "INSERT INTO personnel (fullName, status, roleId) VALUES (?, ?, ?)";
    private static final String INSERT_ROLE = "INSERT INTO role (name) VALUES (?)";

    private static final String SELECT_ALL_PERSONNEL = "SELECT p.*, r.name as RoleName FROM personnel p JOIN role r ON p.roleId = r.id";
    private static final String SELECT_ALL_ROLES = "SELECT * FROM role";

    private static final String UPDATE_PERSONNEL = "UPDATE personnel SET fullName = ?, status = ?, roleId = ? WHERE id = ?";
    private static final String UPDATE_ROLE = "UPDATE role SET name = ? WHERE id = ?";

    private static final String DELETE_PERSONNEL = "DELETE FROM personnel WHERE id = ?";
    private static final String DELETE_ROLE = "DELETE FROM role WHERE id = ?";

    // ==========================================
    // CREATE-операції
    // ==========================================

    // Додає нового співробітника
    public boolean addPersonnel(String fullName, String status, int roleId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_PERSONNEL)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, status);
            pstmt.setInt(3, roleId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при додаванні співробітника: " + e.getMessage());
            return false;
        }
    }

    // Додає нову посаду і повертає її ID
    public int addRoleAndGetId(String name) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_ROLE, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Помилка при додаванні посади: " + e.getMessage());
        }
        return -1;
    }

    // ==========================================
    // READ-операції
    // ==========================================

    // Витягує список персоналу разом з їхніми посадами
    public List<Personnel> getAllPersonnel() {
        List<Personnel> personnelList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_PERSONNEL)) {

            while (rs.next()) {
                Role role = new Role(
                        rs.getInt("roleId"),
                        rs.getString("RoleName")
                );

                Personnel p = new Personnel(
                        rs.getInt("id"),
                        rs.getString("fullName"),
                        rs.getString("status"),
                        role
                );

                personnelList.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні списку персоналу: " + e.getMessage());
        }

        return personnelList;
    }

    // Витягує всі наявні посади
    public List<Role> getAllRoles() {
        List<Role> roles = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_ROLES)) {

            while (rs.next()) {
                roles.add(new Role(
                        rs.getInt("id"),
                        rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні списку посад: " + e.getMessage());
        }

        return roles;
    }

    // ==========================================
    // UPDATE-операції
    // ==========================================

    // Оновлює дані наявного співробітника
    public boolean updatePersonnel(int id, String fullName, String status, int roleId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_PERSONNEL)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, status);
            pstmt.setInt(3, roleId);
            pstmt.setInt(4, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні співробітника: " + e.getMessage());
            return false;
        }
    }

    // Оновлює назву посади
    public boolean updateRole(int id, String name) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_ROLE)) {

            pstmt.setString(1, name);
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при оновленні посади: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // DELETE-операції
    // ==========================================

    // Видаляє співробітника за його ID
    public boolean deletePersonnel(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_PERSONNEL)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні співробітника: " + e.getMessage());
            return false;
        }
    }

    // Видаляє посаду за її ID
    public boolean deleteRole(int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_ROLE)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні посади (можливо, вона ще прив'язана до співробітника): " + e.getMessage());
            return false;
        }
    }
}