package com.gymcrm.storage;

import com.gymcrm.Util.IdGenerator;
import com.gymcrm.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TrainingTypeStorage {

    private final Map<Long, TrainingType> storage = new HashMap<>();
    private IdGenerator idGenerator;

    @Autowired
    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public Map<Long, TrainingType> getStorage() {
        return storage;
    }

    public TrainingType addTrainingType(String name) {
        TrainingType type = new TrainingType(name, idGenerator.generateNextId());
        storage.put(type.getTrainingTypeId(), type);
        return type;
    }

    public TrainingType get(Long id) {
        return storage.get(id);
    }

    public List<TrainingType> findAll() {
        return new ArrayList<>(storage.values());
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
}