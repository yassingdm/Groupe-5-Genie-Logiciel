package eidd.grp5.model;

import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private User client;
    private Room room;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // "CONFIRMED", "CANCELLED"

    public Reservation(User client, Room room, LocalDateTime startTime, LocalDateTime endTime) {
        this.client = client;
        this.room = room;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "CONFIRMED";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getClient() { return client; }
    public Room getRoom() { return room; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isValid() {
        return startTime.isBefore(endTime) && startTime.isAfter(LocalDateTime.now());
    }
}
               

