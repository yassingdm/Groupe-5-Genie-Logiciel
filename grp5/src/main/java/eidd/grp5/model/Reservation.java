package eidd.grp5.model;

import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private String reference;

    private User client;
    private Salle room;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime creationDate;

    private int participantCount;
    private String purpose;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
               

