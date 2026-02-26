package eidd.grp5.model;

public class Room {
    private Long id;
    private String name;
    private int capacity;
    private boolean available;

    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.available = true; // Disponible par défaut
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
