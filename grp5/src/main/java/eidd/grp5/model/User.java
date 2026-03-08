package eidd.grp5.model;

public class User {
    private Long id;
    private String lastName;
    private String firstName;
    private String email;
    private String role; // "ADMIN" ou "CUSTOMER"

    public User(String lastName, String firstName, String email, String role) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Format d'email invalide."); 
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }
}
