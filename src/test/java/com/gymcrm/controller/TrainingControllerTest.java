package com.gymcrm.controller;

import com.gymcrm.dto.AddTrainingRequest;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TrainingControllerTest {

    private GymFacade gymFacade;
    private TrainingController controller;

    @BeforeEach
    void setUp() {
        gymFacade = mock(GymFacade.class);
        controller = new TrainingController(gymFacade);
    }

    @Test
    void addTraining_returns200() {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("John.Doe");
        request.setTrainerUsername("Mike.Brown");
        request.setTrainingName("Morning Cardio");
        request.setTrainingDate(LocalDate.of(2024, 11, 20));
        request.setTrainingDuration(45);

        TrainingType cardio = new TrainingType("Cardio", 1L);
        User trainerUser = new User("Mike", "Brown", "Mike.Brown", "pass", true, 1L);
        Trainer trainer = new Trainer(10L, cardio, 1L);
        trainer.setUser(trainerUser);

        when(gymFacade.matchTraineeCredentials("John.Doe", "pass")).thenReturn(true);
        when(gymFacade.selectTrainerByUsername("Mike.Brown")).thenReturn(trainer);
        when(gymFacade.addTraining("John.Doe", "Mike.Brown", "Morning Cardio", "Cardio",
                LocalDate.of(2024, 11, 20), 45)).thenReturn(new Training());

        ResponseEntity<Void> response = controller.addTraining("John.Doe", "pass", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gymFacade).addTraining("John.Doe", "Mike.Brown", "Morning Cardio", "Cardio",
                LocalDate.of(2024, 11, 20), 45);
    }

    @Test
    void addTraining_unauthorized_returns401() {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("John.Doe");
        request.setTrainerUsername("Mike.Brown");
        request.setTrainingName("Morning Cardio");
        request.setTrainingDate(LocalDate.of(2024, 11, 20));
        request.setTrainingDuration(45);

        when(gymFacade.matchTraineeCredentials("John.Doe", "wrong")).thenReturn(false);

        ResponseEntity<Void> response = controller.addTraining("John.Doe", "wrong", request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(gymFacade, never()).addTraining(any(), any(), any(), any(), any(), any());
    }
}
