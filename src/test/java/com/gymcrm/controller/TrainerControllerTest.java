package com.gymcrm.controller;

import com.gymcrm.actuator.metrics.GymMetrics;
import com.gymcrm.dto.ActivateRequest;
import com.gymcrm.dto.RegistrationResponse;
import com.gymcrm.dto.TrainerProfileResponse;
import com.gymcrm.dto.TrainerRegistrationRequest;
import com.gymcrm.dto.TrainerTrainingListItemDto;
import com.gymcrm.dto.UpdateTrainerProfileRequest;
import com.gymcrm.exceptions.UnauthorizedException;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import com.gymcrm.security.JwtService;
import com.gymcrm.security.SecurityTestUtils;
import com.gymcrm.storage.TrainingTypeStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerControllerTest {

    private GymFacade gymFacade;
    private TrainingTypeStorage trainingTypeStorage;
    private GymMetrics gymMetrics;
    private JwtService jwtService;
    private TrainerController controller;

    @BeforeEach
    void setUp() {
        gymFacade = mock(GymFacade.class);
        trainingTypeStorage = mock(TrainingTypeStorage.class);
        gymMetrics = mock(GymMetrics.class);
        jwtService = mock(JwtService.class);
        controller = new TrainerController(gymFacade, trainingTypeStorage, gymMetrics, jwtService);
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void register_returnsUsernameAndPassword() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Giorgi");
        request.setLastName("Janelidze");
        request.setSpecialization("Cardio");

        TrainingType cardio = new TrainingType("Cardio", 1L);
        when(trainingTypeStorage.requireByName("Cardio")).thenReturn(cardio);

        User user = new User("Giorgi", "Janelidze", "Giorgi.Janelidze", "hashed", true, 1L);
        user.setRawPassword("secret12");
        Trainer trainer = new Trainer();
        trainer.setUser(user);

        when(gymFacade.createTrainer("Giorgi", "Janelidze", cardio)).thenReturn(trainer);
        when(jwtService.generateToken("Giorgi.Janelidze")).thenReturn("jwt-token");

        ResponseEntity<RegistrationResponse> response = controller.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Giorgi.Janelidze", response.getBody().getUsername());
        assertEquals("secret12", response.getBody().getPassword());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getType());
        verify(gymMetrics).trainerRegistered();
    }

    @Test
    void getProfile_returnsProfile() {
        SecurityTestUtils.authenticate("Mike.Brown");

        TrainingType cardio = new TrainingType("Cardio", 1L);
        User user = new User("Mike", "Brown", "Mike.Brown", "pass", true, 1L);
        Trainer trainer = new Trainer(10L, cardio, 1L);
        trainer.setUser(user);

        User traineeUser = new User("Ani", "Smith", "Ani.Smith", "x", true, 2L);
        Trainee trainee = new Trainee(20L, null, null, 2L);
        trainee.setUser(traineeUser);

        Training training = new Training();
        training.setTraineeId(20L);
        training.setTrainerId(10L);

        when(gymFacade.selectTrainerByUsername("Mike.Brown")).thenReturn(trainer);
        when(gymFacade.selectAllTrainees()).thenReturn(Collections.emptyList());
        when(gymFacade.selectTrainingsByTrainerId(10L)).thenReturn(List.of(training));
        when(gymFacade.selectTrainee(20L)).thenReturn(trainee);

        ResponseEntity<TrainerProfileResponse> response = controller.getProfile("Mike.Brown");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Mike", response.getBody().getFirstName());
        assertEquals("Cardio", response.getBody().getSpecialization());
        assertEquals(1, response.getBody().getTraineesList().size());
        assertEquals("Ani.Smith", response.getBody().getTraineesList().get(0).getUsername());
        assertEquals("Ani", response.getBody().getTraineesList().get(0).getFirstName());
        assertEquals("Smith", response.getBody().getTraineesList().get(0).getLastName());
    }

    @Test
    void getProfile_unauthorized_throwsUnauthorized() {
        SecurityTestUtils.authenticate("Someone.Else");

        assertThrows(UnauthorizedException.class,
                () -> controller.getProfile("Mike.Brown"));
        verify(gymFacade, never()).selectTrainerByUsername(any());
    }

    @Test
    void getProfile_withoutAuth_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class,
                () -> controller.getProfile("Mike.Brown"));
        verify(gymFacade, never()).selectTrainerByUsername(any());
    }

    @Test
    void updateProfile_keepsSpecialization() {
        SecurityTestUtils.authenticate("Mike.Brown");

        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("Mike.Brown");
        request.setFirstName("Mike");
        request.setLastName("Updated");
        request.setSpecialization("Yoga"); // ignored
        request.setIsActive(true);

        TrainingType cardio = new TrainingType("Cardio", 1L);
        User user = new User("Mike", "Updated", "Mike.Brown", "pass", true, 1L);
        Trainer trainer = new Trainer(10L, cardio, 1L);
        trainer.setUser(user);

        when(gymFacade.selectTrainerByUsername("Mike.Brown")).thenReturn(trainer);
        when(gymFacade.updateTrainer(10L, "Mike", "Updated", cardio, true)).thenReturn(trainer);
        when(gymFacade.selectAllTrainees()).thenReturn(Collections.emptyList());
        when(gymFacade.selectTrainingsByTrainerId(10L)).thenReturn(Collections.emptyList());

        ResponseEntity<TrainerProfileResponse> response =
                controller.updateProfile("Mike.Brown", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody().getLastName());
        assertEquals("Cardio", response.getBody().getSpecialization());
        verify(gymFacade).updateTrainer(10L, "Mike", "Updated", cardio, true);
    }

    @Test
    void getTrainings_returnsList() {
        SecurityTestUtils.authenticate("Mike.Brown");

        TrainingType cardio = new TrainingType("Cardio", 1L);
        Training training = new Training(1L, 20L, 10L, "Morning Cardio", cardio,
                LocalDate.of(2024, 11, 10), 45);

        User traineeUser = new User("John", "Doe", "John.Doe", "x", true, 2L);
        Trainee trainee = new Trainee(20L, null, null, 2L);
        trainee.setUser(traineeUser);

        when(gymFacade.getTrainerTrainingsList("Mike.Brown", null, null, null))
                .thenReturn(List.of(training));
        when(gymFacade.selectTrainee(20L)).thenReturn(trainee);

        ResponseEntity<List<TrainerTrainingListItemDto>> response =
                controller.getTrainings("Mike.Brown", null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Morning Cardio", response.getBody().get(0).getTrainingName());
        assertEquals("Cardio", response.getBody().get(0).getTrainingType());
        assertEquals(45, response.getBody().get(0).getTrainingDuration());
        assertEquals("John Doe", response.getBody().get(0).getTraineeName());
    }

    @Test
    void getTrainings_unauthorized_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class,
                () -> controller.getTrainings("Mike.Brown", null, null, null));
        verify(gymFacade, never()).getTrainerTrainingsList(any(), any(), any(), any());
    }

    @Test
    void setActive_returns200() {
        SecurityTestUtils.authenticate("Mike.Brown");

        ActivateRequest request = new ActivateRequest();
        request.setUsername("Mike.Brown");
        request.setIsActive(false);

        TrainingType cardio = new TrainingType("Cardio", 1L);
        User user = new User("Mike", "Brown", "Mike.Brown", "pass", true, 1L);
        Trainer trainer = new Trainer(10L, cardio, 1L);
        trainer.setUser(user);

        when(gymFacade.selectTrainerByUsername("Mike.Brown")).thenReturn(trainer);
        when(gymFacade.setTrainerActive(10L, false)).thenReturn(trainer);

        ResponseEntity<Void> response = controller.setActive("Mike.Brown", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gymFacade).setTrainerActive(10L, false);
    }

    @Test
    void setActive_unauthorized_throwsUnauthorized() {
        SecurityTestUtils.authenticate("Someone.Else");

        ActivateRequest request = new ActivateRequest();
        request.setUsername("Mike.Brown");
        request.setIsActive(false);

        assertThrows(UnauthorizedException.class,
                () -> controller.setActive("Mike.Brown", request));
        verify(gymFacade, never()).setTrainerActive(anyLong(), anyBoolean());
    }
}
