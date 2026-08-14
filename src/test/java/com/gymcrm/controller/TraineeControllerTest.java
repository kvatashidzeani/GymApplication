package com.gymcrm.controller;

import com.gymcrm.actuator.metrics.GymMetrics;
import com.gymcrm.dto.ActivateRequest;
import com.gymcrm.dto.RegistrationResponse;
import com.gymcrm.dto.TraineeProfileResponse;
import com.gymcrm.dto.TraineeRegistrationRequest;
import com.gymcrm.dto.TrainerShortDto;
import com.gymcrm.dto.TrainingListItemDto;
import com.gymcrm.dto.UpdateTraineeProfileRequest;
import com.gymcrm.dto.UpdateTraineeTrainersRequest;
import com.gymcrm.exceptions.UnauthorizedException;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeControllerTest {

    private GymFacade gymFacade;
    private GymMetrics gymMetrics;
    private TraineeController controller;

    @BeforeEach
    void setUp() {
        gymFacade = mock(GymFacade.class);
        gymMetrics = mock(GymMetrics.class);
        controller = new TraineeController(gymFacade, gymMetrics);
    }

    @Test
    void register_returnsUsernameAndPassword() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("Ani");
        request.setLastName("Kvatashidze");
        request.setDateOfBirth(LocalDate.of(2005, 6, 9));
        request.setAddress("Gora");

        User user = new User("Ani", "Kvatashidze", "Ani.Kvatashidze", "abc12345", true, 1L);
        Trainee trainee = new Trainee(2L, LocalDate.of(2005, 6, 9), "Gora", 1L);
        trainee.setUser(user);

        when(gymFacade.createTrainee("Ani", "Kvatashidze",
                LocalDate.of(2005, 6, 9), "Gora")).thenReturn(trainee);

        ResponseEntity<RegistrationResponse> response = controller.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ani.Kvatashidze", response.getBody().getUsername());
        assertEquals("abc12345", response.getBody().getPassword());
        verify(gymFacade).createTrainee("Ani", "Kvatashidze",
                LocalDate.of(2005, 6, 9), "Gora");
        verify(gymMetrics).traineeRegistered();
    }

    @Test
    void register_optionalFieldsOmitted() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("Ani");
        request.setLastName("Smith");

        User user = new User("Ani", "Smith", "Ani.Smith", "pass", true, 1L);
        Trainee trainee = new Trainee();
        trainee.setUser(user);

        when(gymFacade.createTrainee("Ani", "Smith", null, null)).thenReturn(trainee);

        ResponseEntity<RegistrationResponse> response = controller.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Ani.Smith", response.getBody().getUsername());
        assertEquals("pass", response.getBody().getPassword());
    }

    @Test
    void getProfile_returnsProfile() {
        User user = new User("Ani", "Smith", "Ani.Smith", "pass", true, 1L);
        Trainee trainee = new Trainee(2L, LocalDate.of(2000, 1, 1), "Tbilisi", 1L);
        trainee.setUser(user);

        when(gymFacade.matchTraineeCredentials("Ani.Smith", "pass")).thenReturn(true);
        when(gymFacade.selectTraineeByUsername("Ani.Smith")).thenReturn(trainee);

        ResponseEntity<TraineeProfileResponse> response = controller.getProfile("Ani.Smith", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ani", response.getBody().getFirstName());
        assertEquals("Smith", response.getBody().getLastName());
        assertEquals("Tbilisi", response.getBody().getAddress());
        assertTrue(response.getBody().getIsActive());
        assertTrue(response.getBody().getTrainersList().isEmpty());
    }

    @Test
    void getProfile_unauthorized_throwsUnauthorized() {
        when(gymFacade.matchTraineeCredentials("Ani.Smith", "wrong")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> controller.getProfile("Ani.Smith", "wrong"));
        verify(gymFacade, never()).selectTraineeByUsername(any());
    }

    @Test
    void updateProfile_returnsUpdatedProfile() {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest();
        request.setUsername("Ani.Smith");
        request.setFirstName("Ani");
        request.setLastName("Updated");
        request.setAddress("Vake");
        request.setIsActive(true);

        User user = new User("Ani", "Updated", "Ani.Smith", "pass", true, 1L);
        Trainee trainee = new Trainee(2L, null, "Vake", 1L);
        trainee.setUser(user);

        when(gymFacade.matchTraineeCredentials("Ani.Smith", "pass")).thenReturn(true);
        when(gymFacade.selectTraineeByUsername("Ani.Smith")).thenReturn(trainee);
        when(gymFacade.updateTrainee(2L, "Ani", "Updated", null, "Vake", true)).thenReturn(trainee);

        ResponseEntity<TraineeProfileResponse> response =
                controller.updateProfile("Ani.Smith", "pass", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Ani.Smith", response.getBody().getUsername());
        assertEquals("Updated", response.getBody().getLastName());
        assertEquals("Vake", response.getBody().getAddress());
        verify(gymFacade).updateTrainee(2L, "Ani", "Updated", null, "Vake", true);
    }

    @Test
    void deleteProfile_returns200() {
        when(gymFacade.matchTraineeCredentials("Ani.Smith", "pass")).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteProfile("Ani.Smith", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gymFacade).deleteTraineeByUsername("Ani.Smith");
    }

    @Test
    void deleteProfile_unauthorized_throwsUnauthorized() {
        when(gymFacade.matchTraineeCredentials("Ani.Smith", "wrong")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> controller.deleteProfile("Ani.Smith", "wrong"));
        verify(gymFacade, never()).deleteTraineeByUsername(any());
    }

    @Test
    void getNotAssignedActiveTrainers_returnsList() {
        TrainingType cardio = new TrainingType("Cardio", 1L);
        User trainerUser = new User("Mike", "Brown", "Mike.Brown", "x", true, 5L);
        Trainer trainer = new Trainer(10L, cardio, 5L);
        trainer.setUser(trainerUser);

        when(gymFacade.matchTraineeCredentials("Ani.Smith", "pass")).thenReturn(true);
        when(gymFacade.getTrainersNotAssignedToTrainee("Ani.Smith")).thenReturn(List.of(trainer));

        ResponseEntity<List<TrainerShortDto>> response =
                controller.getNotAssignedActiveTrainers("Ani.Smith", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Mike.Brown", response.getBody().get(0).getUsername());
        assertEquals("Mike", response.getBody().get(0).getFirstName());
        assertEquals("Brown", response.getBody().get(0).getLastName());
        assertEquals("Cardio", response.getBody().get(0).getSpecialization());
    }

    @Test
    void getNotAssignedActiveTrainers_unauthorized_throwsUnauthorized() {
        when(gymFacade.matchTraineeCredentials("Ani.Smith", "wrong")).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> controller.getNotAssignedActiveTrainers("Ani.Smith", "wrong"));
        verify(gymFacade, never()).getTrainersNotAssignedToTrainee(any());
    }

    @Test
    void updateTrainersList_returnsAssignedTrainers() {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTraineeUsername("Ani.Smith");
        request.setTrainersList(List.of(new UpdateTraineeTrainersRequest.TrainerUsernameDto("Mike.Brown")));

        TrainingType cardio = new TrainingType("Cardio", 1L);
        User trainerUser = new User("Mike", "Brown", "Mike.Brown", "x", true, 5L);
        Trainer trainer = new Trainer(10L, cardio, 5L);
        trainer.setUser(trainerUser);

        User traineeUser = new User("Ani", "Smith", "Ani.Smith", "pass", true, 1L);
        Trainee trainee = new Trainee(2L, LocalDate.of(2000, 1, 1), "Tbilisi", 1L);
        trainee.setUser(traineeUser);
        trainee.setTrainerIds(Set.of(10L));

        when(gymFacade.matchTraineeCredentials("Ani.Smith", "pass")).thenReturn(true);
        when(gymFacade.selectTraineeByUsername("Ani.Smith")).thenReturn(trainee);
        when(gymFacade.selectTrainer(10L)).thenReturn(trainer);

        ResponseEntity<List<TrainerShortDto>> response =
                controller.updateTrainersList("Ani.Smith", "pass", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Mike.Brown", response.getBody().get(0).getUsername());
        assertEquals("Cardio", response.getBody().get(0).getSpecialization());
        verify(gymFacade).updateTraineeTrainersList("Ani.Smith", List.of("Mike.Brown"));
    }

    @Test
    void getTrainings_returnsList() {
        TrainingType cardio = new TrainingType("Cardio", 1L);
        Training training = new Training(1L, 2L, 10L, "Morning Cardio", cardio,
                LocalDate.of(2024, 11, 10), 45);

        User trainerUser = new User("Mike", "Brown", "Mike.Brown", "x", true, 5L);
        Trainer trainer = new Trainer(10L, cardio, 5L);
        trainer.setUser(trainerUser);

        when(gymFacade.matchTraineeCredentials("Ani.Smith", "pass")).thenReturn(true);
        when(gymFacade.getTraineeTrainingsList("Ani.Smith", null, null, null, null))
                .thenReturn(List.of(training));
        when(gymFacade.selectTrainer(10L)).thenReturn(trainer);

        ResponseEntity<List<TrainingListItemDto>> response =
                controller.getTrainings("Ani.Smith", "pass", null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Morning Cardio", response.getBody().get(0).getTrainingName());
        assertEquals("Cardio", response.getBody().get(0).getTrainingType());
        assertEquals(45, response.getBody().get(0).getTrainingDuration());
        assertEquals("Mike Brown", response.getBody().get(0).getTrainerName());
    }

    @Test
    void setActive_returns200() {
        ActivateRequest request = new ActivateRequest();
        request.setUsername("Ani.Smith");
        request.setIsActive(false);

        User user = new User("Ani", "Smith", "Ani.Smith", "pass", true, 1L);
        Trainee trainee = new Trainee(2L, LocalDate.of(2000, 1, 1), "Tbilisi", 1L);
        trainee.setUser(user);

        when(gymFacade.matchTraineeCredentials("Ani.Smith", "pass")).thenReturn(true);
        when(gymFacade.selectTraineeByUsername("Ani.Smith")).thenReturn(trainee);
        when(gymFacade.setTraineeActive(2L, false)).thenReturn(trainee);

        ResponseEntity<Void> response = controller.setActive("Ani.Smith", "pass", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gymFacade).setTraineeActive(2L, false);
    }
}
