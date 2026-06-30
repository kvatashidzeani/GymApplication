package org.example.ServiceTests;


import com.gym.crm.exceptions.TrainingNotFoundException;
import com.gym.crm.Util.IdGenerator;
import com.gym.crm.dao.TrainingDao;
import com.gym.crm.model.Training;
import com.gym.crm.model.TrainingType;
import com.gym.crm.service.TrainingService;
import com.gym.crm.validators.TrainingValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingServiceTests {

    private TrainingDao trainingDao;
    private IdGenerator idGenerator;
    private TrainingService trainingService;
    private TrainingValidator trainingValidator;

    @BeforeEach
    void setup() {
        trainingDao = mock(TrainingDao.class);
        idGenerator = mock(IdGenerator.class);
        trainingValidator = mock(TrainingValidator.class);

        trainingService = new TrainingService();
        trainingService.setTrainingDao(trainingDao);
        trainingService.setIdGenerator(idGenerator);
        trainingService.setTrainingValidator(trainingValidator);
    }

    @Test
    void createTraining_success() {
        TrainingType type = new TrainingType(1L, "Cardio");
        when(idGenerator.generateNextId()).thenReturn(101L);

        Training toSave = new Training();
        toSave.setId(101L);

        when(trainingDao.save(any())).thenReturn(toSave);

        Training result = trainingService.createTraining(
                1L, 2L, "Morning Cardio", type,
                LocalDate.of(2026, 2, 17), 60
        );

        assertEquals(101L, result.getId());
        verify(trainingDao, times(1)).save(any());
    }

    @Test
    void selectTraining_success() {
        Training t = new Training();
        t.setId(10L);
        t.setTrainingName("Evening Yoga");
        when(trainingDao.findById(10L)).thenReturn(Optional.of(t));

        Training result = trainingService.selectTraining(10L);
        assertEquals(10L, result.getId());
        assertEquals("Evening Yoga", result.getTrainingName());
    }

    @Test
    void selectTraining_notFound() {
        when(trainingDao.findById(999L)).thenReturn(Optional.empty());
        assertThrows(TrainingNotFoundException.class, () -> trainingService.selectTraining(999L));
    }

    @Test
    void selectAllTrainings_success() {
        Training t1 = new Training();
        t1.setId(1L);
        Training t2 = new Training();
        t2.setId(2L);
        when(trainingDao.findAll()).thenReturn(List.of(t1, t2));

        List<Training> result = trainingService.selectAllTrainings();
        assertEquals(2, result.size());
    }

    @Test
    void selectTrainingsByTraineeId_success() {
        Training t1 = new Training();
        t1.setTraineeId(1L);
        Training t2 = new Training();
        t2.setTraineeId(1L);
        when(trainingDao.findByTraineeId(1L)).thenReturn(List.of(t1, t2));

        List<Training> result = trainingService.selectTrainingsByTraineeId(1L);
        assertEquals(2, result.size());
    }

    @Test
    void selectTraining_nullId_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> trainingService.selectTraining(null));
    }


    @Test
    void selectTrainingsByTrainerId_success() {
        Training t1 = new Training();
        t1.setTrainerId(2L);
        Training t2 = new Training();
        t2.setTrainerId(2L);
        when(trainingDao.findByTrainerId(2L)).thenReturn(List.of(t1, t2));

        List<Training> result = trainingService.selectTrainingsByTrainerId(2L);
        assertEquals(2, result.size());
    }

    @Test
    void createTraining_invalidInput_shouldThrow() {
        doThrow(new IllegalArgumentException("Invalid training"))
                .when(trainingValidator)
                .validateTraining(any(), any(), any(), any(), any(), anyInt());

        TrainingType type = new TrainingType(1L, "Cardio");

        assertThrows(IllegalArgumentException.class,
                () -> trainingService.createTraining(null, 1L, "Test", type, LocalDate.now(), 60));

        assertThrows(IllegalArgumentException.class,
                () -> trainingService.createTraining(1L, null, "Test", type, LocalDate.now(), 60));

        assertThrows(IllegalArgumentException.class,
                () -> trainingService.createTraining(1L, 1L, "", type, LocalDate.now(), 60));

        assertThrows(IllegalArgumentException.class,
                () -> trainingService.createTraining(1L, 1L, "Test", null, LocalDate.now(), 60));

        assertThrows(IllegalArgumentException.class,
                () -> trainingService.createTraining(1L, 1L, "Test", type, null, 60));

        assertThrows(IllegalArgumentException.class,
                () -> trainingService.createTraining(1L, 1L, "Test", type, LocalDate.now(), 0));
    }
}