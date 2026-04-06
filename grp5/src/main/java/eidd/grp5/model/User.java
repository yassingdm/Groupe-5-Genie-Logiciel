package eidd.grp5.model;

public class User {
    private Long id;
    private String name;
    private String email;

    public User(String name, String email) {
        setName(name); 
        setEmail(email);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'utilisateur est obligatoire.");
        }
        this.name = name;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("L'email de l'utilisateur est obligatoire.");
        }
        this.email = email;
    }
}
