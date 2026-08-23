package com.gymcrm.controller;

import com.gymcrm.dto.TrainingTypeDto;
import com.gymcrm.exceptions.UnauthorizedException;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.TrainingType;
import com.gymcrm.security.SecurityTestUtils;
import com.gymcrm.storage.TrainingTypeStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TrainingTypeControllerTest {

    private TrainingTypeStorage trainingTypeStorage;
    private GymFacade gymFacade;
    private TrainingTypeController controller;

    @BeforeEach
    void setUp() {
        trainingTypeStorage = mock(TrainingTypeStorage.class);
        gymFacade = mock(GymFacade.class);
        controller = new TrainingTypeController(trainingTypeStorage, gymFacade);
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void getTrainingTypes_returnsList() {
        SecurityTestUtils.authenticate("John.Doe");
        when(trainingTypeStorage.findAll()).thenReturn(List.of(
                new TrainingType("Cardio", 1L),
                new TrainingType("Strength", 2L)
        ));

        ResponseEntity<List<TrainingTypeDto>> response = controller.getTrainingTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Cardio", response.getBody().get(0).getTrainingType());
        assertEquals(1L, response.getBody().get(0).getTrainingTypeId());
    }

    @Test
    void getTrainingTypes_unauthorized_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> controller.getTrainingTypes());
        verify(trainingTypeStorage, never()).findAll();
    }
}
