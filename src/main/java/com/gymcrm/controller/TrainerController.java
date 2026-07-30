package com.gymcrm.controller;

import com.gymcrm.dto.ActivateRequest;
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

@Api(tags = "Trainer")
@RestController
@RequestMapping("/trainers")
public class TrainerController {

    private static final Logger log = LoggerFactory.getLogger(TrainerController.class);

    private final GymFacade gymFacade;
    private final TrainingTypeStorage trainingTypeStorage;

    public TrainerController(GymFacade gymFacade, TrainingTypeStorage trainingTypeStorage) {
        this.gymFacade = gymFacade;
        this.trainingTypeStorage = trainingTypeStorage;
    }

    /**
     * 2. Trainer Registration (POST)
     */
    @ApiOperation(
            value = "Trainer Registration",
            notes = "Creates a trainer profile. Specialization must be an existing training type name."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Registration successful"),
            @ApiResponse(code = 400, message = "Invalid request or unknown training type")
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody TrainerRegistrationRequest request) {
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

        RegistrationResponse response = new RegistrationResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getPassword());

        log.info("Trainer registered successfully, username={}", response.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 8. Get Trainer Profile (GET)
     */
    @ApiOperation(
            value = "Get Trainer Profile",
            notes = "Returns trainer profile and assigned trainees. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile found"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @ApiParam(value = "Trainer username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password) {

        log.info("GET /trainers/{}", username);

        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTrainerCredentials(user, pass)) {
            log.warn("Unauthorized get trainer profile for username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Trainer trainer = gymFacade.selectTrainerByUsername(user);
        return ResponseEntity.ok(toProfileResponse(trainer));
    }

    /**
     * 9. Update Trainer Profile (PUT)
     * Specialization is read-only — existing value is kept.
     */
    @ApiOperation(
            value = "Update Trainer Profile",
            notes = "Updates trainer profile. Username and specialization cannot be changed. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile updated"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> updateProfile(
            @ApiParam(value = "Trainer username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password,
            @RequestBody UpdateTrainerProfileRequest request) {

        log.info("PUT /trainers/{}", username);

        RequestValidation.requireNonNull(request, "Request body");
        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTrainerCredentials(user, pass)) {
            log.warn("Unauthorized update trainer profile for username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
            notes = "Sets trainer active status. Fails if already in the requested state. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Status updated"),
            @ApiResponse(code = 400, message = "Invalid request or already in that state"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @PatchMapping("/{username}")
    public ResponseEntity<Void> setActive(
            @ApiParam(value = "Trainer username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password,
            @RequestBody ActivateRequest request) {

        log.info("PATCH /trainers/{} isActive={}", username, request != null ? request.getIsActive() : null);

        RequestValidation.requireNonNull(request, "Request body");
        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTrainerCredentials(user, pass)) {
            log.warn("Unauthorized activate/deactivate for trainer username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
            notes = "Returns trainer trainings. Optional filters: periodFrom, periodTo, traineeName. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainings list returned"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainer not found")
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingListItemDto>> getTrainings(
            @ApiParam(value = "Trainer username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password,
            @ApiParam(value = "Period From (optional)") @RequestParam(value = "periodFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @ApiParam(value = "Period To (optional)") @RequestParam(value = "periodTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @ApiParam(value = "Trainee Name (optional)") @RequestParam(value = "traineeName", required = false) String traineeName) {

        log.info("GET /trainers/{}/trainings from={}, to={}, traineeName={}",
                username, periodFrom, periodTo, traineeName);

        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTrainerCredentials(user, pass)) {
            log.warn("Unauthorized get trainings for trainer username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
        response.setTraineesList(findAssignedTrainees(trainer.getId()));
        return response;
    }

    private List<TraineeShortDto> findAssignedTrainees(Long trainerId) {
        Map<String, TraineeShortDto> byUsername = new LinkedHashMap<>();

        // Trainees explicitly assigned to this trainer (M2M)
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
