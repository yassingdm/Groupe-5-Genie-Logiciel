package eidd.grp5.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import eidd.grp5.util.ValidationUtils;

public class Room {
    private Long id;
    private int capacity;
    private String name;
    private String description;
    private final Set<String> equipments = new LinkedHashSet<>();


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

    public List<String> getEquipments() {
        return new ArrayList<>(equipments);
    }

    public void setEquipments(List<String> newEquipments) {
        equipments.clear();
        if (newEquipments == null) {
            return;
        }
        for (String equipment : newEquipments) {
            addEquipment(equipment);
        }
    }

    public boolean addEquipment(String equipment) {
        if (equipment == null || equipment.isBlank()) {
            throw new IllegalArgumentException("equipment must not be blank");
        }
        return equipments.add(equipment.trim());
    }

    public boolean removeEquipment(String equipment) {
        if (equipment == null || equipment.isBlank()) {
            throw new IllegalArgumentException("equipment must not be blank");
        }
        String target = equipment.trim();
        String existing = equipments.stream()
                .filter(item -> item.equalsIgnoreCase(target))
                .findFirst()
                .orElse(null);
        if (existing == null) {
            return false;
        }
        return equipments.remove(existing);
    }

    public boolean hasEquipment(String equipment) {
        if (equipment == null || equipment.isBlank()) {
            throw new IllegalArgumentException("equipment must not be blank");
        }
        String target = equipment.trim();
        return equipments.stream().anyMatch(item -> item.equalsIgnoreCase(target));
    }
}
