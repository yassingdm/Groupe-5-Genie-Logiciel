package eidd.grp5.repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import eidd.grp5.model.Reservation;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonReservationRepository implements IReservationRepository {
    private static final String FILE_PATH = "reservations.json";
    private final Gson gson;
    private List<Reservation> reservations = new ArrayList<>();

    public JsonReservationRepository() {
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, t, c) -> 
                new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (j, t, c) -> 
                LocalDateTime.parse(j.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .create();
        loadFromFile();
    }

    private void loadFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<Reservation>>(){}.getType();
            reservations = gson.fromJson(reader, listType);
            if (reservations == null) reservations = new ArrayList<>();
        } catch (IOException e) {
            reservations = new ArrayList<>();
        }
    }

    private void saveToFile() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8)) {
            gson.toJson(reservations, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Reservation save(Reservation entity) {
        if (entity.getId() == null) {
            long maxId = reservations.stream().mapToLong(r -> r.getId() != null ? r.getId() : 0).max().orElse(0L);
            entity.setId(maxId + 1);
            reservations.add(entity);
        } else {
            boolean found = false;
            for (int i = 0; i < reservations.size(); i++) {
                if (entity.getId().equals(reservations.get(i).getId())) {
                    reservations.set(i, entity);
                    found = true;
                    break;
                }
            }
            if (!found) reservations.add(entity);
        }
        saveToFile();
        return entity;
    }

    @Override
    public List<Reservation> findAll() { return new ArrayList<>(reservations); }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream().filter(r -> id.equals(r.getId())).findFirst();
    }

    @Override
    public boolean delete(Long id) {
        boolean removed = reservations.removeIf(r -> id.equals(r.getId()));
        if (removed) saveToFile();
        return removed;
    }

    @Override
    public long count() { return reservations.size(); }

    @Override
    public List<Reservation> findByClientId(Long clientId) {
        return reservations.stream().filter(r -> r.getClient() != null && clientId.equals(r.getClient().getId())).toList();
    }

    @Override
    public List<Reservation> findByStatus(Reservation.Status status) {
        return reservations.stream().filter(r -> status.equals(r.getStatus())).toList();
    }

    @Override
    public Optional<Reservation> findByReference(String reference) {
        return reservations.stream().filter(r -> reference.equals(r.getReference())).findFirst();
    }
}
