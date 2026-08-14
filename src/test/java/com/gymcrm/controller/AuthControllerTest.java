package com.gymcrm.controller;

import com.gymcrm.actuator.metrics.GymMetrics;
import com.gymcrm.dto.ChangeLoginRequest;
import com.gymcrm.exceptions.UnauthorizedException;
import com.gymcrm.facade.GymFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private GymFacade gymFacade;
    private GymMetrics gymMetrics;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        gymFacade = mock(GymFacade.class);
        gymMetrics = mock(GymMetrics.class);
        when(gymMetrics.startLoginTimer()).thenReturn(io.micrometer.core.instrument.Timer.start());
        controller = new AuthController(gymFacade, gymMetrics);
    }

    @Test
    void login_traineeCredentials_returns200() {
        when(gymFacade.matchTraineeCredentials("Ani.Smith", "pass")).thenReturn(true);

        ResponseEntity<Void> response = controller.login("Ani.Smith", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gymMetrics).loginSucceeded();
        verify(gymMetrics).stopLoginTimer(any());
    }

    @Test
    void login_trainerCredentials_returns200() {
        when(gymFacade.matchTraineeCredentials("Mike.Brown", "pass")).thenReturn(false);
        when(gymFacade.matchTrainerCredentials("Mike.Brown", "pass")).thenReturn(true);

        ResponseEntity<Void> response = controller.login("Mike.Brown", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gymMetrics).loginSucceeded();
    }

    @Test
    void login_invalidCredentials_throwsUnauthorized() {
        when(gymFacade.matchTraineeCredentials("Ani.Smith", "wrong")).thenReturn(false);
        when(gymFacade.matchTrainerCredentials("Ani.Smith", "wrong")).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> controller.login("Ani.Smith", "wrong"));
        assertEquals("Invalid credentials", ex.getMessage());
        verify(gymMetrics).loginFailed();
        verify(gymMetrics).stopLoginTimer(any());
    }

    @Test
    void login_missingUsername_throws() {
        assertThrows(IllegalArgumentException.class, () -> controller.login("  ", "pass"));
    }

    @Test
    void changeLogin_trainee_returns200() {
        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setUsername("Ani.Smith");
        request.setOldPassword("old");
        request.setNewPassword("new");

        when(gymFacade.matchTraineeCredentials("Ani.Smith", "old")).thenReturn(true);

        ResponseEntity<Void> response = controller.changeLogin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gymFacade).changeTraineePassword("Ani.Smith", "old", "new");
    }

    @Test
    void changeLogin_wrongOldPassword_throwsUnauthorized() {
        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setUsername("Ani.Smith");
        request.setOldPassword("wrong");
        request.setNewPassword("new");

        when(gymFacade.matchTraineeCredentials("Ani.Smith", "wrong")).thenReturn(false);
        when(gymFacade.matchTrainerCredentials("Ani.Smith", "wrong")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> controller.changeLogin(request));
        verify(gymFacade, never()).changeTraineePassword(any(), any(), any());
    }
}
