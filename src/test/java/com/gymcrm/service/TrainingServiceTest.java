package com.gymcrm.service;

import com.gymcrm.Util.IdGenerator;
import com.gymcrm.dao.TrainingDao;
import com.gymcrm.exceptions.TrainingNotFoundException;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.storage.TrainingTypeStorage;
import com.gymcrm.validators.TrainingValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TrainingServiceTest {

    private TrainingService trainingService;
    private TrainingDao trainingDao;
    private IdGenerator idGenerator;
    private TrainingValidator trainingValidator;
    private TraineeService traineeService;
    private TrainerService trainerService;
    private TrainingTypeStorage trainingTypeStorage;

    @BeforeEach
    void setUp() {
        trainingDao = mock(TrainingDao.class);
        idGenerator = mock(IdGenerator.class);
        trainingValidator = mock(TrainingValidator.class);
        traineeService = mock(TraineeService.class);
        trainerService = mock(TrainerService.class);
        trainingTypeStorage = mock(TrainingTypeStorage.class);

        trainingService = new TrainingService();
        trainingService.setTrainingDao(trainingDao);
        trainingService.setIdGenerator(idGenerator);
        trainingService.setTrainingValidator(trainingValidator);
        trainingService.setTraineeService(traineeService);
        trainingService.setTrainerService(trainerService);
        trainingService.setTrainingTypeStorage(trainingTypeStorage);
    }

    @Test
    void createTraining_success() {
        TrainingType cardio = new TrainingType("Cardio", 1L);
        when(idGenerator.generateNextId()).thenReturn(101L);
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

        Training result = trainingService.createTraining(
                1L, 2L, "Morning Cardio", cardio, LocalDate.of(2026, 2, 17), 60);

        assertEquals(101L, result.getId());
        assertEquals("Morning Cardio", result.getTrainingName());
        verify(trainingDao).save(any(Training.class));
    }

    @Test
    void addTraining_resolvesUsernamesAndType() {
        TrainingType cardio = new TrainingType("Cardio", 1L);
        Trainee trainee = new Trainee(1L, LocalDate.of(2000, 1, 1), "Tbilisi", 10L);
        Trainer trainer = new Trainer(2L, cardio, 20L);

        when(traineeService.selectTraineeByUsername("trainee.user")).thenReturn(trainee);
        when(trainerService.selectTrainerByUsername("trainer.user")).thenReturn(trainer);
        when(trainingTypeStorage.requireByName("Cardio")).thenReturn(cardio);
        when(idGenerator.generateNextId()).thenReturn(50L);
        when(trainingDao.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));

        Training result = trainingService.addTraining(
                "trainee.user", "trainer.user", "Session", "Cardio", LocalDate.of(2026, 3, 1), 45);

        assertEquals(50L, result.getId());
        assertEquals(1L, result.getTraineeId());
        assertEquals(2L, result.getTrainerId());
    }

    @Test
    void selectTraining_notFound_throws() {
        when(trainingDao.findById(999L)).thenReturn(Optional.empty());
        assertThrows(TrainingNotFoundException.class, () -> trainingService.selectTraining(999L));
    }

    @Test
    void selectTrainingsByTraineeId_success() {
        Training t1 = new Training();
        t1.setTraineeId(1L);
        when(trainingDao.findByTraineeId(1L)).thenReturn(List.of(t1));

        assertEquals(1, trainingService.selectTrainingsByTraineeId(1L).size());
    }

    @Test
    void createTraining_invalidInput_throws() {
        doThrow(new IllegalArgumentException("Invalid training"))
                .when(trainingValidator)
                .validateTraining(any(), any(), any(), any(), any(), anyInt());

        TrainingType type = new TrainingType("Cardio", 1L);
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.createTraining(null, 1L, "Test", type, LocalDate.now(), 60));
    }
}
