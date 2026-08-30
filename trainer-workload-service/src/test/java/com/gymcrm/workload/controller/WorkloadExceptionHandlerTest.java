package com.gymcrm.workload.controller;

import com.gymcrm.workload.service.TrainerWorkloadNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkloadExceptionHandlerTest {

    private WorkloadExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new WorkloadExceptionHandler();
    }

    @Test
    void handleNotFound_returns404() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(new TrainerWorkloadNotFoundException("missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody().get("error"));
    }

    @Test
    void handleBadRequest_returns400() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleBadRequest(new IllegalArgumentException("month must be between 1 and 12"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("month must be between 1 and 12", response.getBody().get("error"));
    }

    @Test
    void handleMethodNotSupported_returns405() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleMethodNotSupported(
                        new HttpRequestMethodNotSupportedException("POST"));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    void handleOther_returns500() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleOther(new RuntimeException("unexpected"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().get("error"));
    }
}
