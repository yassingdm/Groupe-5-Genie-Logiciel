package eidd.grp5.model;

public class Room {
    private Long id;
    private String name;
    private int capacity;
    

    public Room(String name, int capacity) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la salle ne peut pas être vide.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacité de la salle doit être strictement positive.");
        }

        this.name = name;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
}
