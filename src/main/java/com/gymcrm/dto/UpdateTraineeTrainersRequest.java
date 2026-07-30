package com.gymcrm.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body for Update Trainee's Trainer List (PUT).
 */
public class UpdateTraineeTrainersRequest {

    private String traineeUsername;
    private List<TrainerUsernameDto> trainersList = new ArrayList<>();

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public List<TrainerUsernameDto> getTrainersList() {
        return trainersList;
    }

    public void setTrainersList(List<TrainerUsernameDto> trainersList) {
        this.trainersList = trainersList == null ? new ArrayList<>() : trainersList;
    }

    public static class TrainerUsernameDto {
        private String username;

        public TrainerUsernameDto() {
        }

        public TrainerUsernameDto(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
