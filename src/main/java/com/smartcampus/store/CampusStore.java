package com.smartcampus.store;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class CampusStore {

    private static final Logger LOG = Logger.getLogger(CampusStore.class.getName());

    // Stores the current state of rooms, sensors, and their readings in memory safely for concurrent access
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readingsBySensor = new ConcurrentHashMap<>();

    public List<Room> findAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public Optional<Room> findRoom(String id) {
        return Optional.ofNullable(rooms.get(id));
    }

    /**
     * @return empty if a room with the same id already exists
     */
    // Creates a new room safely, making sure we don't accidentally overwrite an existing one with the same ID
    public Optional<Room> createRoom(Room room) {
        Objects.requireNonNull(room.getId(), "id");
        if (room.getSensorIds() == null) {
            room.setSensorIds(new CopyOnWriteArrayList<>());
        }
        Room copy = new Room(room.getId(), room.getName(), room.getCapacity());
        copy.setSensorIds(new CopyOnWriteArrayList<>(room.getSensorIds()));
        if (rooms.putIfAbsent(copy.getId(), copy) != null) {
            LOG.fine(() -> "Room already exists: " + copy.getId());
            return Optional.empty();
        }
        LOG.info(() -> "Created room " + copy.getId());
        return Optional.of(rooms.get(copy.getId()));
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    /**
     * @return false if the room did not exist
     */
    // Remove a room
    public boolean deleteRoomIfEmpty(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            LOG.fine(() -> "Delete requested for missing room " + roomId);
            return false;
        }
        synchronized (room) {
            if (!room.getSensorIds().isEmpty()) {
                LOG.warning(() -> "Cannot delete room " + roomId + " because it still has assigned sensors");
                throw new RoomNotEmptyException(
                        "Room " + roomId + " still has sensors assigned; remove sensors before decommissioning.");
            }
            rooms.remove(roomId);
        }
        LOG.info(() -> "Deleted room " + roomId);
        return true;
    }

    public List<Sensor> findAllSensors(String typeFilter) {
        List<Sensor> list = new ArrayList<>(sensors.values());
        if (typeFilter == null || typeFilter.isBlank()) {
            return list;
        }
        String needle = typeFilter.trim();
        return list.stream()
                .filter(s -> s.getType() != null && s.getType().equalsIgnoreCase(needle))
                .collect(Collectors.toList());
    }

    public Optional<Sensor> findSensor(String id) {
        return Optional.ofNullable(sensors.get(id));
    }

    /**
     * @return empty if a sensor with the same id is already registered
     */
    // Registers a new sensor and securely links it to its assigned room. Refuses to add if the room doesn't exist
    public Optional<Sensor> registerSensor(Sensor sensor) {
        Objects.requireNonNull(sensor.getId(), "id");
        Objects.requireNonNull(sensor.getRoomId(), "roomId");
        if (!rooms.containsKey(sensor.getRoomId())) {
            LOG.warning(() -> "Sensor registration failed: room does not exist for sensor " + sensor.getId());
            throw new LinkedResourceNotFoundException(
                    "Room '" + sensor.getRoomId() + "' does not exist; cannot register sensor.");
        }
        if (sensors.putIfAbsent(sensor.getId(), sensor) != null) {
            LOG.fine(() -> "Sensor already exists: " + sensor.getId());
            return Optional.empty();
        }
        Room room = rooms.get(sensor.getRoomId());
        synchronized (room) {
            room.getSensorIds().add(sensor.getId());
        }
        LOG.info(() -> "Registered sensor " + sensor.getId() + " in room " + sensor.getRoomId());
        return Optional.of(sensor);
    }

    public List<SensorReading> findReadings(String sensorId) {
        return new ArrayList<>(readingsBySensor.getOrDefault(sensorId, List.of()));
    }

    // Saves a new data reading for a specific sensor. Rejects readings if the sensor is down for maintenance
    public SensorReading appendReading(String sensorId, SensorReading body) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            LOG.fine(() -> "Reading append failed: sensor not found " + sensorId);
            throw new NotFoundException("Sensor '" + sensorId + "' was not found.");
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            LOG.warning(() -> "Reading append rejected: sensor in maintenance " + sensorId);
            throw new SensorUnavailableException(
                    "Sensor " + sensorId + " is in MAINTENANCE and cannot accept new readings.");
        }
        long ts = body.getTimestamp() > 0 ? body.getTimestamp() : System.currentTimeMillis();
        String rid = body.getId() != null && !body.getId().isBlank()
                ? body.getId()
                : UUID.randomUUID().toString();
        SensorReading stored = new SensorReading(rid, ts, body.getValue());
        readingsBySensor
                .computeIfAbsent(sensorId, k -> new CopyOnWriteArrayList<>())
                .add(stored);
        sensor.setCurrentValue(stored.getValue());
        LOG.fine(() -> "Stored reading " + stored.getId() + " for sensor " + sensorId);
        return stored;
    }
}
