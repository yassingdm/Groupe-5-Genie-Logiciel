package eidd.grp5.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private User client;
    private Room room;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status; 

    public Reservation(User client, Room room, LocalDateTime startTime, LocalDateTime endTime) {
        this.client = client;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ReservationStatus.CONFIRMED; 
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getClient() { return client; }
    public Room getRoom() { return room; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    
    public boolean isValid() {
        if (startTime == null || endTime == null) return false;
        return startTime.isBefore(endTime) && startTime.isAfter(LocalDateTime.now());
    }

    public long getDuration() {
        if (startTime == null || endTime == null) return 0;
        return Duration.between(startTime, endTime).toMinutes();
    }
}
               

