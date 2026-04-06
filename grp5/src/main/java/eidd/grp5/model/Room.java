package eidd.grp5.model;

public class Room {
    private Long id;
    private int capacity;
    private String name;
    private String description;

    public Room(int id, String name, int capacity, String description) {
        this.id = (long) id;
        setName(name);
        setCapacity(capacity);
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacité d'une salle doit être strictement positive.");
        }
        this.capacity = capacity;
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la salle est obligatoire.");
        }
        this.name = name;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
