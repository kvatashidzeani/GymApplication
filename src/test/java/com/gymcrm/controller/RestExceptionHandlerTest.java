package com.gymcrm.controller;

import com.gymcrm.dto.ErrorResponse;
import com.gymcrm.exceptions.AccountLockedException;
import com.gymcrm.exceptions.TraineeNotFoundException;
import com.gymcrm.exceptions.TrainerNotFoundException;
import com.gymcrm.exceptions.TrainingNotFoundException;
import com.gymcrm.exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.junit.jupiter.api.Assertions.*;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
    }

    @Test
    void handleIllegalArgument_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalArgument(new IllegalArgumentException("bad input"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad input", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleIllegalState_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIllegalState(new IllegalStateException("already active"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("already active", response.getBody().getError());
    }

    @Test
    void handleTraineeNotFound_returns404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleTraineeNotFound(new TraineeNotFoundException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody().getError());
    }

    @Test
    void handleTrainerNotFound_returns404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleTrainerNotFound(new TrainerNotFoundException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleTrainingNotFound_returns404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleTrainingNotFound(new TrainingNotFoundException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleUnauthorized_returns401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnauthorized(new UnauthorizedException("Invalid credentials"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody().getError());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void handleAccountLocked_returns429() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAccountLocked(new AccountLockedException("User is blocked for 5 minutes"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("User is blocked for 5 minutes", response.getBody().getError());
        assertEquals(429, response.getBody().getStatus());
    }

    @Test
    void handleMissingParam_returns400() throws Exception {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("password", "String");
        ResponseEntity<ErrorResponse> response = handler.handleMissingParam(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getError().contains("password"));
    }

    @Test
    void handleUnreadableBody_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnreadableBody(new HttpMessageNotReadableException("invalid json"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleMethodNotSupported_returns405() {
        ResponseEntity<ErrorResponse> response =
                handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("DELETE"));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    void handleUnexpected_returns500() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().getError());
    }
}
