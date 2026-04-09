package eidd.grp5.model;   

import eidd.grp5.util.ValidationUtils;

public class Room {
    private Long id;
    private int capacity;
    private String name;
    private String description;


    public Room(int id,String name,int capacity,String description){
        this.id=(long)ValidationUtils.requireNonNegative(id, "id");
        this.name=ValidationUtils.requireNonBlank(name, "name");
        this.capacity=ValidationUtils.requireNonNegative(capacity, "capacity");
        this.description=ValidationUtils.requireNonNull(description, "description must not be null");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
