package eidd.grp5.model;   
public class Room {
    private Long id;
    private int capacity;
    private String name;
    private boolean available;
    private String description;


    public Room(int id,String name,int capacity,String description){
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
}
