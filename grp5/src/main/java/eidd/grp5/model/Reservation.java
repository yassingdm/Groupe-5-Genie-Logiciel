package eidd.grp5.model;

import java.time.LocalDateTime;

public class Reservation {
private Long id;                 
    private String reference;         
    
    
    private User client;              
    private Salle salle;              
    
    
    private LocalDateTime dateDebut;  
    private LocalDateTime dateFin;    
    private LocalDateTime dateCreation; 
    
    
    private int nombreParticipants;    
    private String motif;             
}    

