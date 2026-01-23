package eidd.grp5.modele;   
public class Salle {
    private int id;
    private int capacite;
    private String nom;
    private boolean disponible;
    private String description;


    public Salle(int id,String nom,int capacite,String description){
        this.id=id;
        this.nom=nom;
        this.capacite=capacite;
        this.description=description;
    }
}
