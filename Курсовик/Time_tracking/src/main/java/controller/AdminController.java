package controller;

import model.TimeRecord;
import model.Leave;
import model.User;
import util.DatabaseConnector;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminController {
    public static ResultSet getAllEmployees() throws SQLException {
        String sql = "SELECT id, name, login FROM users WHERE role = 'EMPLOYEE'";
        Connection conn = DatabaseConnector.getConnection();
        return conn.createStatement().executeQuery(sql);
    }

    public static ResultSet getAllLeaveRequests() throws SQLException {
        String sql = "SELECT l.*, u.name as user_name FROM leaves l " +
                "JOIN users u ON l.user_id = u.id " +
                "WHERE l.status = 'PENDING'";
        Connection conn = DatabaseConnector.getConnection();
        return conn.createStatement().executeQuery(sql);
    }

    public static boolean updateLeaveStatus(int leaveId, String status) {
        String sql = "UPDATE leaves SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, leaveId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<TimeRecord> getEmployeeTimeRecords(int userId) {
        List<TimeRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM time_records WHERE user_id = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                records.add(new TimeRecord(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null,
                        rs.getTimestamp("pause_start") != null ? rs.getTimestamp("pause_start").toLocalDateTime() : null,
                        rs.getTimestamp("pause_end") != null ? rs.getTimestamp("pause_end").toLocalDateTime() : null,
                        rs.getInt("total_time")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static List<Leave> getEmployeeLeaves(int userId) {
        List<Leave> leaves = new ArrayList<>();
        String sql = "SELECT * FROM leaves WHERE user_id = ? AND status = 'APPROVED'";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                leaves.add(new Leave(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getString("comment"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaves;
    }

    public static HashMap<User, List<TimeRecord>> getAllTimeRecords() {
        HashMap<User, List<TimeRecord>> allRecords = new HashMap<>();
        try {
            ResultSet users = getAllEmployees();
            while (users.next()) {
                User user = new User(
                        users.getInt("id"),
                        users.getString("name"),
                        users.getString("login"),
                        "EMPLOYEE"
                );
                List<TimeRecord> records = getEmployeeTimeRecords(user.getId());
                allRecords.put(user, records); // Добавляем даже пустые списки
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allRecords;
    }

    public static HashMap<User, List<Leave>> getAllLeaves() {
        HashMap<User, List<Leave>> allLeaves = new HashMap<>();
        try {
            ResultSet users = getAllEmployees();
            while (users.next()) {
                User user = new User(
                        users.getInt("id"),
                        users.getString("name"),
                        users.getString("login"),
                        "EMPLOYEE"
                );
                List<Leave> leaves = getEmployeeLeaves(user.getId());
                allLeaves.put(user, leaves); // Добавляем даже пустые списки
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return allLeaves;
    }
}