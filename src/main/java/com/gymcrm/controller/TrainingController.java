package com.gymcrm.controller;

import com.gymcrm.dto.AddTrainingRequest;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.validators.RequestValidation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Training")
@RestController
@RequestMapping("/trainings")
public class TrainingController {

    private static final Logger log = LoggerFactory.getLogger(TrainingController.class);

    private final GymFacade gymFacade;

    public TrainingController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    /**
     * 14. Add Training (POST)
     * Training type is taken from the trainer's specialization.
     */
    @ApiOperation(
            value = "Add Training",
            notes = "Creates a training. Training type is derived from the trainer specialization. "
                    + "Password must match the trainee or trainer."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Training created"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee or trainer not found")
    })
    @PostMapping
    public ResponseEntity<Void> addTraining(
            @ApiParam(value = "Authenticated username (trainee or trainer)", required = true)
            @RequestParam("username") String username,
            @ApiParam(value = "Password", required = true)
            @RequestParam("password") String password,
            @RequestBody AddTrainingRequest request) {

        log.info("POST /trainings authUser={}, trainee={}, trainer={}, name={}",
                username, request != null ? request.getTraineeUsername() : null,
                request != null ? request.getTrainerUsername() : null,
                request != null ? request.getTrainingName() : null);

        RequestValidation.requireNonNull(request, "Request body");
        String authUsername = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        String traineeUsername = RequestValidation.requireNonBlank(request.getTraineeUsername(), "Trainee username");
        String trainerUsername = RequestValidation.requireNonBlank(request.getTrainerUsername(), "Trainer username");
        RequestValidation.requireNonBlank(request.getTrainingName(), "Training name");
        RequestValidation.requireNonNull(request.getTrainingDate(), "Training date");
        RequestValidation.requireNonNull(request.getTrainingDuration(), "Training duration");
        if (request.getTrainingDuration() <= 0) {
            throw new IllegalArgumentException("Training duration is required and must be positive");
        }

        boolean authorized = (authUsername.equals(traineeUsername)
                && gymFacade.matchTraineeCredentials(traineeUsername, pass))
                || (authUsername.equals(trainerUsername)
                && gymFacade.matchTrainerCredentials(trainerUsername, pass));
        if (!authorized) {
            log.warn("Unauthorized add training for authUser={}, trainee={}, trainer={}",
                    authUsername, traineeUsername, trainerUsername);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

        log.info("Training created: '{}' for trainee {} with trainer {}",
                request.getTrainingName(), traineeUsername, trainerUsername);
        return ResponseEntity.ok().build();
    }
}
