package com.gymcrm.storage;

import com.gymcrm.model.Trainer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TrainerStorage {
    private Map<Long, Trainer> trainerMap = new HashMap<>();

   public Map<Long,Trainer> getStorage(){
       return trainerMap;
   }

}