package eidd.grp5.model;

import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private String reference;

    private User client;
    private Room room;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime creationDate;

    private int participantCount;
    private String purpose;

    public enum Status {
        PENDING,
        CONFIRMED,
        CANCELLED
    }

    private Status status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
               

