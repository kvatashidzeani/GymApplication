package com.gym.crm.storage;

import com.gym.crm.model.Training;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TrainingStorage {
    private Map<Long, Training> trainingMap = new HashMap<>();

    public Map<Long,Training> getStorage(){
        return trainingMap;
    }
}