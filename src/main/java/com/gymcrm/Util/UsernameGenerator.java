package com.gym.crm.Util;

import com.gym.crm.dao.TraineeDao;
import com.gym.crm.dao.TrainerDao;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class UsernameGenerator {
    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);
    private TrainerDao trainerDao;
    private TraineeDao traineeDao;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    public String generateUsername(String firstname, String lastname){
       String base = firstname.trim() + "." + lastname.trim();
       String username = base;
       int counter = 1;
       Set<String> existingUsernames = getAllUsernames();
       while(existingUsernames.contains(username)){
           username = base + counter;
           counter ++;
           log.debug("Username {} already exists,trying {}",base,username);
       }
       existingUsernames.add(username);
       log.debug("Generated username {}",username);
       return username;
    }

    public Set<String> getAllUsernames(){
        Set<String> usernames = new HashSet<>();
        usernames.addAll(
                Stream.concat(
                        trainerDao.findAll().stream().map(Trainer::getUsername),
                        traineeDao.findAll().stream().map(Trainee::getUsername)
                ).collect(Collectors.toSet())
        );

        return usernames;
    }
}