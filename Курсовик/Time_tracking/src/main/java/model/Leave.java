package model;

import java.time.LocalDate;

public class Leave {
    private int id;
    private int userId;
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String comment;
    private String status;

    public Leave(int id, int userId, String type, LocalDate startDate,
                 LocalDate endDate, String comment, String status) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.comment = comment;
        this.status = status;
    }

    // Геттеры
    public int getId() { return id; }
    public String getType() { return type; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getComment() { return comment; }
    public String getStatus() { return status; }
}