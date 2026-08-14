package com.gymcrm.controller;

import com.gymcrm.dto.ErrorResponse;
import com.gymcrm.dto.TrainingTypeDto;
import com.gymcrm.exceptions.UnauthorizedException;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.TrainingType;
import com.gymcrm.storage.TrainingTypeStorage;
import com.gymcrm.validators.RequestValidation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Api(value = "Training Type API", tags = "Training Type", description = "Training type catalog operations")
@RestController
@RequestMapping("/training-types")
public class TrainingTypeController {

    private static final Logger log = LoggerFactory.getLogger(TrainingTypeController.class);

    private final TrainingTypeStorage trainingTypeStorage;
    private final GymFacade gymFacade;

    public TrainingTypeController(TrainingTypeStorage trainingTypeStorage, GymFacade gymFacade) {
        this.trainingTypeStorage = trainingTypeStorage;
        this.gymFacade = gymFacade;
    }

    /**
     * 17. Get Training types (GET) — requires trainee or trainer authentication.
     */
    @ApiOperation(
            value = "Get Training types",
            notes = "Returns all available training types. Requires username and password authentication.",
            response = TrainingTypeDto.class,
            responseContainer = "List"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training types list", response = TrainingTypeDto.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResponse.class)
    })
    @GetMapping
    public ResponseEntity<List<TrainingTypeDto>> getTrainingTypes(
            @ApiParam(value = "Username (trainee or trainer)", required = true) @RequestParam("username") String username,
            @ApiParam(value = "Password", required = true) @RequestParam("password") String password) {

        log.info("GET /training-types username={}", username);

        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);

        boolean authorized = gymFacade.matchTraineeCredentials(user, pass)
                || gymFacade.matchTrainerCredentials(user, pass);
        if (!authorized) {
            log.warn("Unauthorized get training types for username={}", user);
            throw new UnauthorizedException("Unauthorized");
        }

        List<TrainingTypeDto> result = new ArrayList<>();
        for (TrainingType type : trainingTypeStorage.findAll()) {
            result.add(new TrainingTypeDto(type.getTrainingTypeName(), type.getTrainingTypeId()));
        }
        return ResponseEntity.ok(result);
    }
}
