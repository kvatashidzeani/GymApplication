package com.gymcrm.controller;

import com.gymcrm.dto.ErrorResponse;
import com.gymcrm.dto.TrainingTypeDto;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.TrainingType;
import com.gymcrm.security.SecurityUtils;
import com.gymcrm.storage.TrainingTypeStorage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Training Type API", tags = "Training Type", description = "Training type catalog operations")
@RestController
@RequestMapping("/training-types")
public class TrainingTypeController {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeController.class);

    private final TrainingTypeStorage trainingTypeStorage;

    public TrainingTypeController(TrainingTypeStorage trainingTypeStorage, GymFacade gymFacade) {
        this.trainingTypeStorage = trainingTypeStorage;
    }

    /**
     * 17. Get Training types (GET) — requires Spring Security authentication.
     */
    @ApiOperation(
            value = "Get Training types",
            notes = "Returns all available training types. Requires JWT Bearer authentication.",
            response = TrainingTypeDto.class,
            responseContainer = "List"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training types list", response = TrainingTypeDto.class, responseContainer = "List"),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResponse.class)
    })
    @GetMapping
    public ResponseEntity<List<TrainingTypeDto>> getTrainingTypes() {
        String user = SecurityUtils.currentUsername();
        log.info("GET /training-types username={}", user);

        List<TrainingTypeDto> result = new ArrayList<>();
        for (TrainingType type : trainingTypeStorage.findAll()) {
            result.add(new TrainingTypeDto(type.getTrainingTypeName(), type.getTrainingTypeId()));
        }
        return ResponseEntity.ok(result);
    }
}
