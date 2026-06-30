package org.example.FacadeTest;

import com.gym.crm.facade.GymFacade;
import com.gym.crm.model.*;
import com.gym.crm.Util.IdGenerator;
import com.gym.crm.service.TraineeService;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GymFacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private GymFacade gymFacade;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private Training training;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        trainingType = new TrainingType(1L, "Strength");
        trainee = new Trainee();
        trainee.setId(1L);
        trainee.setFirstName("dato");
        trainee.setLastName("jincharadze");
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
        trainee.setAddress("Tbilisi");

        trainer = new Trainer();
        trainer.setId(1L);
        trainer.setFirstName("ikako");
        trainer.setLastName("deisadze");
        trainer.setSpecialization(trainingType);

        training = new Training();
        training.setId(1L);
        training.setTraineeId(trainee.getId());
        training.setTrainerId(trainer.getId());
        training.setTrainingType(trainingType);
        training.setTrainingName("cardio");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(60);
    }

    @Test
    void createTraineeDelegatesToTraineeService() {
        when(traineeService.createTrainee(anyString(), anyString(), any(), anyString())).thenReturn(trainee);

        Trainee result = gymFacade.createTrainee("dato", "jincharadze", LocalDate.of(2000, 1, 1), "Tbilisi");

        assertNotNull(result);
        assertEquals(trainee.getId(), result.getId());
        verify(traineeService, times(1)).createTrainee("dato", "jincharadze", LocalDate.of(2000, 1, 1), "Tbilisi");
    }

    @Test
    void createTrainerDelegatesToTrainerService() {
        when(trainerService.createTrainer(anyString(), anyString(), any())).thenReturn(trainer);

        Trainer result = gymFacade.createTrainer("gio", "janelidze", trainingType);

        assertNotNull(result);
        assertEquals(trainer.getId(), result.getId());
        verify(trainerService, times(1)).createTrainer("gio", "janelidze", trainingType);
    }

    @Test
    void createTrainingDelegatesToTrainingService() {
        when(trainingService.createTraining(anyLong(), anyLong(), anyString(), any(), any(), anyInt()))
                .thenReturn(training);

        Training result = gymFacade.createTraining(
                trainee.getId(),
                trainer.getId(),
                "cardio",
                trainingType,
                LocalDate.now(),
                60
        );

        assertNotNull(result);
        assertEquals(training.getId(), result.getId());
        verify(trainingService, times(1))
                .createTraining(trainee.getId(), trainer.getId(), "cardio", trainingType, training.getTrainingDate(), 60);
    }

    @Test
    void selectTraineeDelegatesToTraineeService() {
        when(traineeService.select(anyLong())).thenReturn(trainee);

        Trainee result = gymFacade.selectTrainee(1L);

        assertEquals(trainee.getId(), result.getId());
        verify(traineeService, times(1)).select(1L);
    }

    @Test
    void selectTrainerDelegatesToTrainerService() {
        when(trainerService.selectTrainer(anyLong())).thenReturn(trainer);

        Trainer result = gymFacade.selectTrainer(1L);

        assertEquals(trainer.getId(), result.getId());
        verify(trainerService, times(1)).selectTrainer(1L);
    }

    @Test
    void selectTrainingDelegatesToTrainingService() {
        when(trainingService.selectTraining(anyLong())).thenReturn(training);

        Training result = gymFacade.selectTraining(1L);

        assertEquals(training.getId(), result.getId());
        verify(trainingService, times(1)).selectTraining(1L);
    }

    @Test
    void selectAllTraineesDelegates() {
        when(traineeService.selectAllTrainees()).thenReturn(List.of(trainee));

        List<Trainee> result = gymFacade.selectAllTrainees();

        assertEquals(1, result.size());
        verify(traineeService, times(1)).selectAllTrainees();
    }

    @Test
    void selectAllTrainersDelegates() {
        when(trainerService.selectAllTrainers()).thenReturn(List.of(trainer));

        List<Trainer> result = gymFacade.selectAllTrainers();

        assertEquals(1, result.size());
        verify(trainerService, times(1)).selectAllTrainers();
    }

    @Test
    void selectAllTrainingsDelegates() {
        when(trainingService.selectAllTrainings()).thenReturn(List.of(training));

        List<Training> result = gymFacade.selectAllTrainings();

        assertEquals(1, result.size());
        verify(trainingService, times(1)).selectAllTrainings();
    }
}
