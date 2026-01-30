package eidd.grp5.modele;   
public class Salle {
    private int id;
    private int capacity;
    private String name;
    private boolean available;
    private String description;


    public Salle(int id,String name,int capacity,String description){
        this.id=id;
        this.name=name;
        this.capacity=capacity;
        this.description=description;
    }
}
