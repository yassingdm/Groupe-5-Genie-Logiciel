package eidd.grp5.model;

import eidd.grp5.util.ValidationUtils;

public class User {
    public enum Role {
        CUSTOMER,
        ADMIN
    }

    private Long id;
    private String name;
    private String email;
    private Role role;

    public User(String name,String email){
        this.name=ValidationUtils.requireNonBlank(name, "name");
        this.email=ValidationUtils.requireNonBlank(email, "email");
        this.role=Role.CUSTOMER;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
