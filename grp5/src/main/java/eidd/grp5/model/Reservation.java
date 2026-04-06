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

    
    public Reservation(Room room, LocalDateTime startDate, LocalDateTime endDate) {
        if (room == null) {
            throw new IllegalArgumentException("La salle est obligatoire pour une réservation.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Les dates de début et de fin sont obligatoires.");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }
        
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.creationDate = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    
    public Reservation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public User getClient() {
        return client;
    }

    public void setClient(User client) {
        this.client = client;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        
        if (this.endDate != null && startDate != null && !this.endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin.");
        }
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        
        if (this.startDate != null && endDate != null && !endDate.isAfter(this.startDate)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }
        this.endDate = endDate;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        if (participantCount < 0) {
            throw new IllegalArgumentException("Le nombre de participants ne peut pas être négatif.");
        }
        this.participantCount = participantCount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
               

