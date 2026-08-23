package com.gymcrm.controller;

import com.gymcrm.actuator.metrics.GymMetrics;
import com.gymcrm.dto.ActivateRequest;
import com.gymcrm.dto.ErrorResponse;
import com.gymcrm.dto.RegistrationResponse;
import com.gymcrm.dto.TraineeShortDto;
import com.gymcrm.dto.TrainerProfileResponse;
import com.gymcrm.dto.TrainerRegistrationRequest;
import com.gymcrm.dto.TrainerTrainingListItemDto;
import com.gymcrm.dto.UpdateTrainerProfileRequest;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.model.User;
import com.gymcrm.security.JwtService;
import com.gymcrm.security.SecurityUtils;
import com.gymcrm.storage.TrainingTypeStorage;
import com.gymcrm.validators.RequestValidation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Api(value = "Trainer API", tags = "Trainer", description = "Trainer registration and profile operations")
@RestController
@RequestMapping("/trainers")
public class TrainerController {

    private static final Logger log = LoggerFactory.getLogger(TrainerController.class);

    private final GymFacade gymFacade;
    private final TrainingTypeStorage trainingTypeStorage;
    private final GymMetrics gymMetrics;
    private final JwtService jwtService;

    public TrainerController(GymFacade gymFacade, TrainingTypeStorage trainingTypeStorage, GymMetrics gymMetrics,
                             JwtService jwtService) {
        this.gymFacade = gymFacade;
        this.trainingTypeStorage = trainingTypeStorage;
        this.gymMetrics = gymMetrics;
        this.jwtService = jwtService;
    }

    /**
     * 2. Trainer Registration (POST) — Create Profile.
     */
    @ApiOperation(
            value = "Trainer Registration",
            notes = "Creates a trainer profile and returns credentials plus a JWT Bearer token.",
            response = RegistrationResponse.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Registration successful", response = RegistrationResponse.class),
            @ApiResponse(code = 400, message = "Invalid request or unknown training type", response = ErrorResponse.class)
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @ApiParam(value = "Trainer registration request", required = true)
            @RequestBody TrainerRegistrationRequest request) {
        RequestValidation.requireNonNull(request, "Request body");
        RequestValidation.requireNonBlank(request.getFirstName(), "First name");
        RequestValidation.requireNonBlank(request.getLastName(), "Last name");
        RequestValidation.requireNonBlank(request.getSpecialization(), "Specialization");

        log.info("POST /trainers/register firstName={}, lastName={}, specialization={}",
                request.getFirstName(), request.getLastName(), request.getSpecialization());

        TrainingType specialization = trainingTypeStorage.requireByName(request.getSpecialization().trim());

        Trainer trainer = gymFacade.createTrainer(
                request.getFirstName(),
                request.getLastName(),
                specialization);

        String username = trainer.getUser().getUsername();
        String rawPassword = trainer.getUser().getRawPassword();
        String token = jwtService.generateToken(username);
        trainer.getUser().setRawPassword(null);

        RegistrationResponse response = new RegistrationResponse(username, rawPassword, token);

        gymMetrics.trainerRegistered();
        log.info("Trainer registered successfully, username={} (JWT issued)", username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 8. Get Trainer Profile (GET)
     */
    @ApiOperation(
            value = "Get Trainer Profile",
            notes = "Returns trainer profile and assigned trainees. Requires JWT Bearer authentication.",
            response = TrainerProfileResponse.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile found", response = TrainerProfileResponse.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Trainer not found", response = ErrorResponse.class)
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @ApiParam(value = "Trainer username", required = true) @PathVariable("username") String username) {

        log.info("GET /trainers/{}", username);

        String user = SecurityUtils.requireSelf(RequestValidation.requireUsername(username));

        Trainer trainer = gymFacade.selectTrainerByUsername(user);
        return ResponseEntity.ok(toProfileResponse(trainer));
    }

    /**
     * 9. Update Trainer Profile (PUT)
     * Specialization is read-only — existing value is kept.
     */
    @ApiOperation(
            value = "Update Trainer Profile",
            notes = "Updates trainer profile. Username and specialization cannot be changed. Requires JWT Bearer authentication.",
            response = TrainerProfileResponse.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile updated", response = TrainerProfileResponse.class),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Trainer not found", response = ErrorResponse.class)
    })
    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> updateProfile(
            @ApiParam(value = "Trainer username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Updated trainer profile fields", required = true)
            @RequestBody UpdateTrainerProfileRequest request) {

        log.info("PUT /trainers/{}", username);

        RequestValidation.requireNonNull(request, "Request body");
        String user = SecurityUtils.requireSelf(RequestValidation.requireUsername(username));
        RequestValidation.requireSameUsername(user, request.getUsername());
        RequestValidation.requireNonBlank(request.getFirstName(), "First name");
        RequestValidation.requireNonBlank(request.getLastName(), "Last name");
        RequestValidation.requireNonNull(request.getIsActive(), "Is Active");

        Trainer existing = gymFacade.selectTrainerByUsername(user);
        // specialization is read-only — keep existing
        gymFacade.updateTrainer(
                existing.getId(),
                request.getFirstName(),
                request.getLastName(),
                existing.getSpecialization(),
                request.getIsActive());

        Trainer refreshed = gymFacade.selectTrainerByUsername(user);
        log.info("Trainer profile updated for username={}", user);
        return ResponseEntity.ok(toProfileResponse(refreshed));
    }

    /**
     * 16. Activate / De-Activate Trainer (PATCH) — non-idempotent.
     */
    @ApiOperation(
            value = "Activate / De-Activate Trainer",
            notes = "Sets trainer active status. Fails if already in the requested state. Requires JWT Bearer authentication.",
            response = Void.class
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Status updated"),
            @ApiResponse(code = 400, message = "Invalid request or already in that state", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Trainer not found", response = ErrorResponse.class)
    })
    @PatchMapping("/{username}")
    public ResponseEntity<Void> setActive(
            @ApiParam(value = "Trainer username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Activate/deactivate request with username and isActive flag", required = true)
            @RequestBody ActivateRequest request) {

        log.info("PATCH /trainers/{} isActive={}", username, request != null ? request.getIsActive() : null);

        RequestValidation.requireNonNull(request, "Request body");
        String user = SecurityUtils.requireSelf(RequestValidation.requireUsername(username));
        RequestValidation.requireSameUsername(user, request.getUsername());
        RequestValidation.requireNonNull(request.getIsActive(), "Is Active");

        Trainer trainer = gymFacade.selectTrainerByUsername(user);
        gymFacade.setTrainerActive(trainer.getId(), request.getIsActive());
        log.info("Trainer {} isActive set to {}", user, request.getIsActive());
        return ResponseEntity.ok().build();
    }

    /**
     * 13. Get Trainer Trainings List (GET)
     */
    @ApiOperation(
            value = "Get Trainer Trainings List",
            notes = "Returns trainer trainings. Optional filters: periodFrom, periodTo, traineeName. Requires JWT Bearer authentication.",
            response = TrainerTrainingListItemDto.class,
            responseContainer = "List"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainings list returned", response = TrainerTrainingListItemDto.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Trainer not found", response = ErrorResponse.class)
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingListItemDto>> getTrainings(
            @ApiParam(value = "Trainer username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Period From (optional)") @RequestParam(value = "periodFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @ApiParam(value = "Period To (optional)") @RequestParam(value = "periodTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @ApiParam(value = "Trainee Name (optional)") @RequestParam(value = "traineeName", required = false) String traineeName) {

        log.info("GET /trainers/{}/trainings from={}, to={}, traineeName={}",
                username, periodFrom, periodTo, traineeName);

        String user = SecurityUtils.requireSelf(RequestValidation.requireUsername(username));
        RequestValidation.requirePeriodOrder(periodFrom, periodTo);

        List<TrainerTrainingListItemDto> result = new ArrayList<>();
        for (Training training : gymFacade.getTrainerTrainingsList(
                user, periodFrom, periodTo, traineeName)) {
            result.add(toTrainerTrainingItem(training));
        }
        return ResponseEntity.ok(result);
    }

    private TrainerTrainingListItemDto toTrainerTrainingItem(Training training) {
        String typeName = training.getTrainingType() != null
                ? training.getTrainingType().getTrainingTypeName()
                : null;
        String traineeName = null;
        if (training.getTraineeId() != null) {
            Trainee trainee = gymFacade.selectTrainee(training.getTraineeId());
            if (trainee.getUser() != null) {
                traineeName = (trainee.getUser().getFirstName() + " " + trainee.getUser().getLastName()).trim();
            }
        }
        return new TrainerTrainingListItemDto(
                training.getTrainingName(),
                training.getTrainingDate(),
                typeName,
                training.getTrainingDuration(),
                traineeName);
    }

    private TrainerProfileResponse toProfileResponse(Trainer trainer) {
        User user = trainer.getUser();
        TrainerProfileResponse response = new TrainerProfileResponse();
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setSpecialization(trainer.getSpecialization() != null
                ? trainer.getSpecialization().getTrainingTypeName()
                : null);
        response.setIsActive(user.isActive());
        response.setTraineesList(findAssignedTrainees(trainer));
        return response;
    }

    private List<TraineeShortDto> findAssignedTrainees(Trainer trainer) {
        Long trainerId = trainer.getId();
        Map<String, TraineeShortDto> byUsername = new LinkedHashMap<>();

        Trainer withLinks = gymFacade.selectTrainer(trainerId);
        if (withLinks == null) {
            withLinks = trainer;
        }

        // Inverse side of many-to-many (Trainer.traineeIds)
        if (withLinks.getTraineeIds() != null) {
            for (Long traineeId : withLinks.getTraineeIds()) {
                putTraineeShort(byUsername, gymFacade.selectTrainee(traineeId));
            }
        }

        // Owning side scan (Trainee.trainerIds) — keeps lists consistent if only one side was set
        for (Trainee trainee : gymFacade.selectAllTrainees()) {
            if (trainee.getTrainerIds() != null && trainee.getTrainerIds().contains(trainerId)) {
                putTraineeShort(byUsername, trainee);
            }
        }

        // Trainees who have trainings with this trainer (seed / training history)
        for (Training training : gymFacade.selectTrainingsByTrainerId(trainerId)) {
            putTraineeShort(byUsername, gymFacade.selectTrainee(training.getTraineeId()));
        }

        return new ArrayList<>(byUsername.values());
    }

    private void putTraineeShort(Map<String, TraineeShortDto> byUsername, Trainee trainee) {
        if (trainee == null) {
            return;
        }
        User traineeUser = trainee.getUser();
        if (traineeUser == null || traineeUser.getUsername() == null) {
            return;
        }
        byUsername.putIfAbsent(traineeUser.getUsername(), new TraineeShortDto(
                traineeUser.getUsername(),
                traineeUser.getFirstName(),
                traineeUser.getLastName()));
    }
}
