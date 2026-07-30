package com.gymcrm.controller;

import com.gymcrm.dto.ChangeLoginRequest;
import com.gymcrm.facade.GymFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private GymFacade gymFacade;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        gymFacade = mock(GymFacade.class);
        controller = new AuthController(gymFacade);
    }

    @Test
    void login_traineeCredentials_returns200() {
        when(gymFacade.matchTraineeCredentials("Ani.Smith", "pass")).thenReturn(true);

        ResponseEntity<Void> response = controller.login("Ani.Smith", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void login_trainerCredentials_returns200() {
        when(gymFacade.matchTraineeCredentials("Mike.Brown", "pass")).thenReturn(false);
        when(gymFacade.matchTrainerCredentials("Mike.Brown", "pass")).thenReturn(true);

        ResponseEntity<Void> response = controller.login("Mike.Brown", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void login_invalidCredentials_returns401() {
        when(gymFacade.matchTraineeCredentials("Ani.Smith", "wrong")).thenReturn(false);
        when(gymFacade.matchTrainerCredentials("Ani.Smith", "wrong")).thenReturn(false);

        ResponseEntity<Void> response = controller.login("Ani.Smith", "wrong");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
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
    void changeLogin_wrongOldPassword_returns401() {
        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setUsername("Ani.Smith");
        request.setOldPassword("wrong");
        request.setNewPassword("new");

        when(gymFacade.matchTraineeCredentials("Ani.Smith", "wrong")).thenReturn(false);
        when(gymFacade.matchTrainerCredentials("Ani.Smith", "wrong")).thenReturn(false);

        ResponseEntity<Void> response = controller.changeLogin(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(gymFacade, never()).changeTraineePassword(any(), any(), any());
    }
}
