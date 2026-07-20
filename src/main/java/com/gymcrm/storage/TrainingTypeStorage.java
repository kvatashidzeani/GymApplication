package com.gymcrm.storage;

import com.gymcrm.Util.IdGenerator;
import com.gymcrm.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class TrainingTypeStorage {

    private final Map<Long, TrainingType> storage = new HashMap<>();
    private IdGenerator idGenerator;

    @Autowired
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Map<Long, TrainingType> getStorage() {
        return Collections.unmodifiableMap(storage);
    }

    /**
     * Seed-only: loads constant training types from initial data. Not for runtime app use.
     */
    public TrainingType seedTrainingType(String name) {
        TrainingType type = new TrainingType(name, idGenerator.generateNextId());
        storage.put(type.getTrainingTypeId(), type);
        return type;
    }

    public TrainingType get(Long id) {
        return storage.get(id);
    }

    public List<TrainingType> findAll() {
        return List.copyOf(storage.values());
    }

    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Map<String, TrainingType> getAllByName() {
        Map<String, TrainingType> typesByName = new HashMap<>();
        for (TrainingType type : storage.values()) {
            typesByName.put(type.getTrainingTypeName(), type);
        }
        return Collections.unmodifiableMap(typesByName);
    }

    public TrainingType requireByName(String name) {
        TrainingType type = getAllByName().get(name);
        if (type == null) {
            throw new IllegalArgumentException("Training type not found: " + name);
        }
        return type;
    }
}
