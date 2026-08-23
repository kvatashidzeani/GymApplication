package com.gymcrm.controller;

import com.gymcrm.actuator.metrics.GymMetrics;
import com.gymcrm.dto.ChangeLoginRequest;
import com.gymcrm.dto.JwtResponse;
import com.gymcrm.exceptions.AccountLockedException;
import com.gymcrm.exceptions.UnauthorizedException;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.security.JwtService;
import com.gymcrm.security.LoginAttemptService;
import com.gymcrm.security.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private GymFacade gymFacade;
    private GymMetrics gymMetrics;
    private AuthenticationManager authenticationManager;
    private LoginAttemptService loginAttemptService;
    private JwtService jwtService;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        gymFacade = mock(GymFacade.class);
        gymMetrics = mock(GymMetrics.class);
        authenticationManager = mock(AuthenticationManager.class);
        loginAttemptService = mock(LoginAttemptService.class);
        jwtService = mock(JwtService.class);
        when(gymMetrics.startLoginTimer()).thenReturn(io.micrometer.core.instrument.Timer.start());
        controller = new AuthController(
                gymFacade, gymMetrics, authenticationManager, loginAttemptService, jwtService);
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void login_validCredentials_returnsJwt() {
        Authentication authenticated = new UsernamePasswordAuthenticationToken(
                User.withUsername("Ani.Smith").password("secret").roles("TRAINEE").build(),
                "secret",
                List.of());
        when(loginAttemptService.isBlocked("Ani.Smith")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticated);
        when(jwtService.generateToken(any(org.springframework.security.core.userdetails.UserDetails.class)))
                .thenReturn("jwt-token");

        ResponseEntity<JwtResponse> response = controller.login("Ani.Smith", "secret");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getType());
        assertEquals("Ani.Smith", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(loginAttemptService).loginSucceeded("Ani.Smith");
        verify(gymMetrics).loginSucceeded();
        verify(gymMetrics, never()).loginFailed();
        verify(gymMetrics).stopLoginTimer(any());
    }

    @Test
    void login_invalidCredentials_throwsUnauthorized_andRecordsFailure() {
        when(loginAttemptService.isBlocked("Ani.Smith")).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> controller.login("Ani.Smith", "wrong"));
        verify(loginAttemptService).loginFailed("Ani.Smith");
        verify(jwtService, never()).generateToken(anyString());
        verify(gymMetrics, never()).loginSucceeded();
        verify(gymMetrics).loginFailed();
        verify(gymMetrics).stopLoginTimer(any());
    }

    @Test
    void login_whenBlocked_throwsAccountLocked() {
        when(loginAttemptService.isBlocked("Ani.Smith")).thenReturn(true);

        assertThrows(AccountLockedException.class, () -> controller.login("Ani.Smith", "secret"));
        verify(authenticationManager, never()).authenticate(any());
        verify(loginAttemptService, never()).loginFailed(any());
        verify(gymMetrics).loginFailed();
    }

    @Test
    void login_blankUsername_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> controller.login("  ", "secret"));
        verify(authenticationManager, never()).authenticate(any());
        verify(gymMetrics, never()).loginSucceeded();
        verify(gymMetrics).stopLoginTimer(any());
    }

    @Test
    void changeLogin_trainee_returns200() {
        SecurityTestUtils.authenticate("Ani.Smith");

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
    void changeLogin_trainer_returns200() {
        SecurityTestUtils.authenticate("Mike.Brown");

        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setUsername("Mike.Brown");
        request.setOldPassword("old");
        request.setNewPassword("new");

        when(gymFacade.matchTraineeCredentials("Mike.Brown", "old")).thenReturn(false);
        when(gymFacade.matchTrainerCredentials("Mike.Brown", "old")).thenReturn(true);

        ResponseEntity<Void> response = controller.changeLogin(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(gymFacade).changeTrainerPassword("Mike.Brown", "old", "new");
    }

    @Test
    void changeLogin_wrongOldPassword_throwsUnauthorized() {
        SecurityTestUtils.authenticate("Ani.Smith");

        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setUsername("Ani.Smith");
        request.setOldPassword("wrong");
        request.setNewPassword("new");

        when(gymFacade.matchTraineeCredentials("Ani.Smith", "wrong")).thenReturn(false);
        when(gymFacade.matchTrainerCredentials("Ani.Smith", "wrong")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> controller.changeLogin(request));
        verify(gymFacade, never()).changeTraineePassword(any(), any(), any());
        verify(gymFacade, never()).changeTrainerPassword(any(), any(), any());
    }

    @Test
    void changeLogin_withoutAuth_throwsUnauthorized() {
        ChangeLoginRequest request = new ChangeLoginRequest();
        request.setUsername("Ani.Smith");
        request.setOldPassword("old");
        request.setNewPassword("new");

        assertThrows(UnauthorizedException.class, () -> controller.changeLogin(request));
        verify(gymFacade, never()).changeTraineePassword(any(), any(), any());
    }
}
