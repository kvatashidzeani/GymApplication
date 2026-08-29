package com.gymcrm.controller;

import com.gymcrm.actuator.metrics.GymMetrics;
import com.gymcrm.dto.AddTrainingRequest;
import com.gymcrm.dto.ErrorResponse;
import com.gymcrm.exceptions.UnauthorizedException;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.security.SecurityUtils;
import com.gymcrm.validators.RequestValidation;
import io.micrometer.core.instrument.Timer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Training API", tags = "Training", description = "Training session management operations")
@RestController
@RequestMapping("/trainings")
public class TrainingController {

    private static final Logger log = LoggerFactory.getLogger(TrainingController.class);

    private final GymFacade gymFacade;
    private final GymMetrics gymMetrics;

    public TrainingController(GymFacade gymFacade, GymMetrics gymMetrics) {
        this.gymFacade = gymFacade;
        this.gymMetrics = gymMetrics;
    }

    /**
     * 14. Add Training (POST)
     * Training type is taken from the trainer's specialization.
     * Authenticated user (JWT Bearer) must be the trainee or the trainer in the request.
     */
    @ApiOperation(
            value = "Add Training",
            notes = "Creates a training. Training type is derived from the trainer specialization. "
                    + "Requires JWT Bearer auth as the trainee or trainer.",
            response = Void.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training created"),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Trainee or trainer not found", response = ErrorResponse.class)
    })
    @PostMapping
    public ResponseEntity<Void> addTraining(
            @ApiParam(value = "Training creation request", required = true)
            @RequestBody AddTrainingRequest request) {

        String authUsername = SecurityUtils.currentUsername();
        log.info("POST /trainings authUser={}, trainee={}, trainer={}, name={}",
                authUsername, request != null ? request.getTraineeUsername() : null,
                request != null ? request.getTrainerUsername() : null,
                request != null ? request.getTrainingName() : null);

        Timer.Sample sample = gymMetrics.startTrainingCreateTimer();
        try {
            RequestValidation.requireNonNull(request, "Request body");
            String traineeUsername = RequestValidation.requireNonBlank(request.getTraineeUsername(), "Trainee username");
            String trainerUsername = RequestValidation.requireNonBlank(request.getTrainerUsername(), "Trainer username");
            RequestValidation.requireNonBlank(request.getTrainingName(), "Training name");
            RequestValidation.requireNonNull(request.getTrainingDate(), "Training date");
            RequestValidation.requireNonNull(request.getTrainingDuration(), "Training duration");
            if (request.getTrainingDuration() <= 0) {
                throw new IllegalArgumentException("Training duration is required and must be positive");
            }

            boolean authorized = authUsername.equals(traineeUsername) || authUsername.equals(trainerUsername);
            if (!authorized) {
                log.warn("Unauthorized add training for authUser={}, trainee={}, trainer={}",
                        authUsername, traineeUsername, trainerUsername);
                throw new UnauthorizedException(
                        "JWT user '" + authUsername + "' must match traineeUsername or trainerUsername "
                                + "(got trainee='" + traineeUsername + "', trainer='" + trainerUsername + "')");
            }

            Trainer trainer = gymFacade.selectTrainerByUsername(trainerUsername);
            TrainingType specialization = trainer.getSpecialization();
            if (specialization == null || specialization.getTrainingTypeName() == null) {
                throw new IllegalArgumentException("Trainer has no specialization; cannot determine training type");
            }

            gymFacade.addTraining(
                    traineeUsername,
                    trainerUsername,
                    request.getTrainingName().trim(),
                    specialization.getTrainingTypeName(),
                    request.getTrainingDate(),
                    request.getTrainingDuration());

            gymMetrics.trainingCreated();
            log.info("Training created: '{}' for trainee {} with trainer {}",
                    request.getTrainingName(), traineeUsername, trainerUsername);
            return ResponseEntity.ok().build();
        } finally {
            gymMetrics.stopTrainingCreateTimer(sample);
        }
    }
}
