package eidd.grp5.model;   
public class Room {
    private Long id;
    private int capacity;
    private String name;
    private String description;


    public Room(int id,String name,int capacity,String description){
        if (id < 0) {
            throw new IllegalArgumentException("id must be >= 0");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must be >= 0");
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null");
        }
        this.id=(long)id;
        this.name=name;
        this.capacity=capacity;
        this.description=description;
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
