package model;

import java.time.LocalDateTime;

public class TimeRecord {
    private int id;
    private int userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime pauseStart;
    private LocalDateTime pauseEnd;
    private int totalSeconds;

    public TimeRecord(int id, int userId, LocalDateTime startTime, LocalDateTime endTime,
                      LocalDateTime pauseStart, LocalDateTime pauseEnd, int totalSeconds) {
        this.id = id;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pauseStart = pauseStart;
        this.pauseEnd = pauseEnd;
        this.totalSeconds = totalSeconds;
    }

    // Getters
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getTotalSeconds() { return totalSeconds; }
}