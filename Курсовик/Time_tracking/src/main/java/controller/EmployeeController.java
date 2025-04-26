package controller;

import model.Leave;
import model.TimeRecord;
import util.DatabaseConnector;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeController {

    // Методы управления рабочим временем
    public static boolean startWorkDay(int userId) {
        String sql = "INSERT INTO time_records (user_id, date, start_time) VALUES (?, CURDATE(), NOW())";
        return executeUpdate(userId, sql);
    }

    public static boolean startPause(int userId) {
        String sql = "UPDATE time_records SET pause_start = NOW() " +
                "WHERE user_id = ? AND date = CURDATE() AND end_time IS NULL";
        return executeUpdate(userId, sql);
    }

    public static boolean endPause(int userId) {
        String sql = "UPDATE time_records SET pause_end = NOW() " +
                "WHERE user_id = ? AND date = CURDATE() AND end_time IS NULL";
        return executeUpdate(userId, sql);
    }

    public static boolean endWorkDay(int userId) {
        String sql = "UPDATE time_records SET end_time = NOW(), total_time = " +
                "TIMESTAMPDIFF(SECOND, start_time, NOW()) - " +
                "IFNULL(TIMESTAMPDIFF(SECOND, pause_start, pause_end), 0) " +
                "WHERE user_id = ? AND date = CURDATE() AND NOW() >= start_time";
        return executeUpdate(userId, sql);
    }

    // Методы для отпусков/больничных
    public static boolean requestLeave(int userId, String type, LocalDate start, LocalDate end, String comment) {
        if (start.isAfter(end)) {
            System.err.println("Дата начала отпуска не может быть позже даты окончания");
            return false;
        }

        String sql = "INSERT INTO leaves (user_id, type, start_date, end_date, comment) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, type);
            stmt.setDate(3, Date.valueOf(start));
            stmt.setDate(4, Date.valueOf(end));
            stmt.setString(5, comment);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Получение данных
    public static List<TimeRecord> getTimeRecords(int userId) {
        List<TimeRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM time_records WHERE user_id = ? ORDER BY date DESC";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new TimeRecord(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getTimestamp("start_time").toLocalDateTime(),
                            rs.getTimestamp("end_time") != null ?
                                    rs.getTimestamp("end_time").toLocalDateTime() : null,
                            rs.getTimestamp("pause_start") != null ?
                                    rs.getTimestamp("pause_start").toLocalDateTime() : null,
                            rs.getTimestamp("pause_end") != null ?
                                    rs.getTimestamp("pause_end").toLocalDateTime() : null,
                            rs.getInt("total_time")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static List<Leave> getLeaves(int userId) {
        List<Leave> leaves = new ArrayList<>();
        String sql = "SELECT * FROM leaves WHERE user_id = ?";

        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    leaves.add(new Leave(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("type"),
                            rs.getDate("start_date").toLocalDate(),
                            rs.getDate("end_date").toLocalDate(),
                            rs.getString("comment")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return leaves;
    }

    // Вспомогательный метод
    private static boolean executeUpdate(int userId, String sql) {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean hasActiveWorkDay(int userId) {
        String sql = "SELECT id FROM time_records WHERE user_id = ? AND end_time IS NULL";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isPauseActive(int userId) {
        String sql = "SELECT pause_start FROM time_records " +
                "WHERE user_id = ? AND end_time IS NULL AND pause_start IS NOT NULL AND pause_end IS NULL";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}