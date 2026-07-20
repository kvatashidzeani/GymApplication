package com.gymcrm.facade;

import com.gymcrm.model.*;
import com.gymcrm.service.TraineeService;
import com.gymcrm.service.TrainerService;
import com.gymcrm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GymFacadeTest {

    @Mock private TraineeService traineeService;
    @Mock private TrainerService trainerService;
    @Mock private TrainingService trainingService;

    @InjectMocks private GymFacade gymFacade;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private Training training;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User traineeUser = new User("Ani", "Kvatashidze", "Ani.Kvatashidze", "pass", true, 10L);
        trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        trainee.setUser(traineeUser);

        trainingType = new TrainingType("Strength", 1L);
        User trainerUser = new User("Medea", "Alfaidze", "Medea.Alfaidze", "pass", true, 20L);
        trainer = new Trainer(2L, trainingType, 20L);
        trainer.setUser(trainerUser);

        training = new Training(3L, 1L, 2L, "cardio", trainingType,
                LocalDate.of(2024, 11, 10), 60);
    }

    @Test
    void createTraineeDelegatesToService() {
        when(traineeService.createTrainee(anyString(), anyString(), any(), anyString())).thenReturn(trainee);

        Trainee result = gymFacade.createTrainee("Ani", "Kvatashidze", LocalDate.of(2000, 1, 1), "Tbilisi");

        assertEquals(1L, result.getId());
        verify(traineeService).createTrainee("Ani", "Kvatashidze", LocalDate.of(2000, 1, 1), "Tbilisi");
    }

    @Test
    void createTrainerDelegatesToService() {
        when(trainerService.createTrainer(anyString(), anyString(), any())).thenReturn(trainer);

        Trainer result = gymFacade.createTrainer("Medea", "Alfaidze", trainingType);

        assertEquals(2L, result.getId());
        verify(trainerService).createTrainer("Medea", "Alfaidze", trainingType);
    }

    @Test
    void deleteTraineeByUsernameDelegatesToService() {
        gymFacade.deleteTraineeByUsername("Ani.Kvatashidze");
        verify(traineeService).deleteTraineeByUsername("Ani.Kvatashidze");
    }

    @Test
    void matchTraineeCredentialsDelegatesToService() {
        when(traineeService.matchTraineeCredentials("Ani.Kvatashidze", "pass")).thenReturn(true);
        assertTrue(gymFacade.matchTraineeCredentials("Ani.Kvatashidze", "pass"));
    }

    @Test
    void addTrainingDelegatesToService() {
        when(trainingService.addTraining(anyString(), anyString(), anyString(), anyString(), any(), anyInt()))
                .thenReturn(training);

        Training result = gymFacade.addTraining(
                "Ani.Kvatashidze", "Medea.Alfaidze", "cardio", "Strength",
                LocalDate.of(2024, 11, 10), 60);

        assertEquals(3L, result.getId());
        verify(trainingService).addTraining(
                "Ani.Kvatashidze", "Medea.Alfaidze", "cardio", "Strength",
                LocalDate.of(2024, 11, 10), 60);
    }

    @Test
    void getTrainersNotAssignedToTraineeDelegatesToService() {
        when(traineeService.getTrainersNotAssignedToTrainee("Ani.Kvatashidze"))
                .thenReturn(List.of(trainer));

        assertEquals(1, gymFacade.getTrainersNotAssignedToTrainee("Ani.Kvatashidze").size());
    }

    @Test
    void selectAllTrainingsDelegates() {
        when(trainingService.selectAllTrainings()).thenReturn(List.of(training));
        assertEquals(1, gymFacade.selectAllTrainings().size());
    }
}
