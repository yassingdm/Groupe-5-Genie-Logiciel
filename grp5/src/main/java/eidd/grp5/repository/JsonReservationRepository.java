package eidd.grp5.repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import eidd.grp5.model.Reservation;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class JsonReservationRepository implements IReservationRepository {
    private static final String FILE_PATH = "reservations.json";
    private Gson gson;
    private List<Reservation> reservations = new ArrayList<>();

    public JsonReservationRepository() {
        
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> 
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> 
                LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();
        
        loadFromFile();
    }

    private void loadFromFile() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<ArrayList<Reservation>>(){}.getType();
            reservations = gson.fromJson(reader, listType);
            if (reservations == null) reservations = new ArrayList<>();
        } catch (FileNotFoundException e) {
            reservations = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(reservations, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Reservation save(Reservation entity) {
        if (entity.getId() == null) {
            entity.setId(reservations.isEmpty() ? 1L : reservations.get(reservations.size() - 1).getId() + 1);
            reservations.add(entity);
        } else {
            for (int i = 0; i < reservations.size(); i++) {
                if (reservations.get(i).getId().equals(entity.getId())) {
                    reservations.set(i, entity);
                    break;
                }
            }
        }
        saveToFile();
        return entity;
    }

    @Override
    public List<Reservation> findAll() { return new ArrayList<>(reservations); }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream().filter(r -> r.getId().equals(id)).findFirst();
    }

    @Override
    public boolean delete(Long id) {
        boolean removed = reservations.removeIf(r -> r.getId().equals(id));
        if (removed) saveToFile();
        return removed;
    }

    @Override
    public long count() { return reservations.size(); }

    

    @Override
    public List<Reservation> findByClientId(Long clientId) {
        return filterReservations(r -> r.getClient() != null && clientId.equals(r.getClient().getId()));
    }

    @Override
    public List<Reservation> findByStatus(Reservation.Status status) {
        return filterReservations(r -> status.equals(r.getStatus()));
    }

    @Override
    public Optional<Reservation> findByReference(String reference) {
        return reservations.stream().filter(r -> reference.equals(r.getReference())).findFirst();
    }

    private List<Reservation> filterReservations(Predicate<Reservation> predicate) {
        return reservations.stream().filter(predicate).toList();
    }
}
