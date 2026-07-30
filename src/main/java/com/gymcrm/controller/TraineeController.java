package com.gymcrm.controller;

import com.gymcrm.dto.ActivateRequest;
import com.gymcrm.dto.RegistrationResponse;
import com.gymcrm.dto.TraineeProfileResponse;
import com.gymcrm.dto.TraineeRegistrationRequest;
import com.gymcrm.dto.TrainerShortDto;
import com.gymcrm.dto.TrainingListItemDto;
import com.gymcrm.dto.UpdateTraineeProfileRequest;
import com.gymcrm.dto.UpdateTraineeTrainersRequest;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.User;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.List;

@Api(tags = "Trainee")
@RestController
@RequestMapping("/trainees")
public class TraineeController {

    private static final Logger log = LoggerFactory.getLogger(TraineeController.class);

    private final GymFacade gymFacade;

    public TraineeController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    /**
     * 1. Trainee Registration (POST)
     * Request: firstName, lastName (required); dateOfBirth, address (optional)
     * Response: username, password
     */
    @ApiOperation(
            value = "Trainee Registration",
            notes = "Creates a trainee profile. Username and password are generated automatically."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Registration successful"),
            @ApiResponse(code = 400, message = "Invalid request (missing required fields)")
    })
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody TraineeRegistrationRequest request) {
        RequestValidation.requireNonNull(request, "Request body");
        RequestValidation.requireNonBlank(request.getFirstName(), "First name");
        RequestValidation.requireNonBlank(request.getLastName(), "Last name");

        log.info("POST /trainees/register firstName={}, lastName={}",
                request.getFirstName(), request.getLastName());

        Trainee trainee = gymFacade.createTrainee(
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                request.getAddress());

        RegistrationResponse response = new RegistrationResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getPassword());

        log.info("Trainee registered successfully, username={}", response.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 5. Get Trainee Profile (GET)
     * Request: username (path). Password query param required for authentication (assignment note 3).
     * Response: profile fields + trainers list
     */
    @ApiOperation(
            value = "Get Trainee Profile",
            notes = "Returns trainee profile. Requires matching username/password (authentication)."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile found"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @ApiParam(value = "Trainee username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password) {

        log.info("GET /trainees/{}", username);

        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTraineeCredentials(user, pass)) {
            log.warn("Unauthorized get trainee profile for username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Trainee trainee = gymFacade.selectTraineeByUsername(user);
        return ResponseEntity.ok(toProfileResponse(trainee));
    }

    /**
     * 6. Update Trainee Profile (PUT)
     * Request: username, firstName, lastName, isActive (required); dateOfBirth, address (optional)
     * Response: updated profile including trainers list
     */
    @ApiOperation(
            value = "Update Trainee Profile",
            notes = "Updates trainee profile. Username cannot be changed. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile updated"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> updateProfile(
            @ApiParam(value = "Trainee username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password,
            @RequestBody UpdateTraineeProfileRequest request) {

        log.info("PUT /trainees/{}", username);

        RequestValidation.requireNonNull(request, "Request body");
        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTraineeCredentials(user, pass)) {
            log.warn("Unauthorized update trainee profile for username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RequestValidation.requireSameUsername(user, request.getUsername());
        RequestValidation.requireNonBlank(request.getFirstName(), "First name");
        RequestValidation.requireNonBlank(request.getLastName(), "Last name");
        RequestValidation.requireNonNull(request.getIsActive(), "Is Active");

        Trainee existing = gymFacade.selectTraineeByUsername(user);
        gymFacade.updateTrainee(
                existing.getId(),
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                request.getAddress(),
                request.getIsActive());

        Trainee refreshed = gymFacade.selectTraineeByUsername(user);
        log.info("Trainee profile updated for username={}", user);
        return ResponseEntity.ok(toProfileResponse(refreshed));
    }

    /**
     * 7. Delete Trainee Profile (DELETE)
     * Hard delete: removes Trainee, linked User, and cascades related trainings.
     */
    @ApiOperation(
            value = "Delete Trainee Profile",
            notes = "Hard-deletes trainee profile and cascades training deletion. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainee deleted"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteProfile(
            @ApiParam(value = "Trainee username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password) {

        log.info("DELETE /trainees/{}", username);

        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTraineeCredentials(user, pass)) {
            log.warn("Unauthorized delete trainee profile for username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        gymFacade.deleteTraineeByUsername(user);
        log.info("Trainee deleted: {}", user);
        return ResponseEntity.ok().build();
    }

    /**
     * 10. Get not assigned on trainee active trainers (GET)
     */
    @ApiOperation(
            value = "Get not assigned active trainers",
            notes = "Returns active trainers that are not assigned to this trainee. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "List returned"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping("/{username}/trainers/not-assigned")
    public ResponseEntity<List<TrainerShortDto>> getNotAssignedActiveTrainers(
            @ApiParam(value = "Trainee username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password) {

        log.info("GET /trainees/{}/trainers/not-assigned", username);

        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTraineeCredentials(user, pass)) {
            log.warn("Unauthorized get not-assigned trainers for username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<TrainerShortDto> trainers = new ArrayList<>();
        for (Trainer trainer : gymFacade.getTrainersNotAssignedToTrainee(user)) {
            User trainerUser = trainer.getUser();
            String specialization = trainer.getSpecialization() != null
                    ? trainer.getSpecialization().getTrainingTypeName()
                    : null;
            trainers.add(new TrainerShortDto(
                    trainerUser.getUsername(),
                    trainerUser.getFirstName(),
                    trainerUser.getLastName(),
                    specialization));
        }

        return ResponseEntity.ok(trainers);
    }

    /**
     * 11. Update Trainee's Trainer List (PUT)
     */
    @ApiOperation(
            value = "Update Trainee's Trainer List",
            notes = "Replaces the trainee's assigned trainers. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainers list updated"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee or trainer not found")
    })
    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerShortDto>> updateTrainersList(
            @ApiParam(value = "Trainee username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password,
            @RequestBody UpdateTraineeTrainersRequest request) {

        log.info("PUT /trainees/{}/trainers", username);

        RequestValidation.requireNonNull(request, "Request body");
        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTraineeCredentials(user, pass)) {
            log.warn("Unauthorized update trainers list for username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RequestValidation.requireSameUsername(user, request.getTraineeUsername());
        RequestValidation.requireNonNull(request.getTrainersList(), "Trainers list");

        List<String> trainerUsernames = new ArrayList<>();
        for (UpdateTraineeTrainersRequest.TrainerUsernameDto item : request.getTrainersList()) {
            RequestValidation.requireNonNull(item, "Trainer list item");
            trainerUsernames.add(RequestValidation.requireNonBlank(item.getUsername(), "Trainer username"));
        }

        gymFacade.updateTraineeTrainersList(user, trainerUsernames);

        Trainee refreshed = gymFacade.selectTraineeByUsername(user);
        List<TrainerShortDto> response = toTrainersList(refreshed);
        log.info("Updated trainers list for trainee {}: {} trainer(s)", user, response.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 12. Get Trainee Trainings List (GET)
     */
    @ApiOperation(
            value = "Get Trainee Trainings List",
            notes = "Returns trainee trainings. Optional filters: periodFrom, periodTo, trainerName, trainingType. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Trainings list returned"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingListItemDto>> getTrainings(
            @ApiParam(value = "Trainee username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password,
            @ApiParam(value = "Period From (optional)") @RequestParam(value = "periodFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @ApiParam(value = "Period To (optional)") @RequestParam(value = "periodTo", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @ApiParam(value = "Trainer Name (optional)") @RequestParam(value = "trainerName", required = false) String trainerName,
            @ApiParam(value = "Training Type (optional)") @RequestParam(value = "trainingType", required = false) String trainingType) {

        log.info("GET /trainees/{}/trainings from={}, to={}, trainerName={}, trainingType={}",
                username, periodFrom, periodTo, trainerName, trainingType);

        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTraineeCredentials(user, pass)) {
            log.warn("Unauthorized get trainings for username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RequestValidation.requirePeriodOrder(periodFrom, periodTo);

        List<TrainingListItemDto> result = new ArrayList<>();
        for (Training training : gymFacade.getTraineeTrainingsList(
                user, periodFrom, periodTo, trainerName, trainingType)) {
            result.add(toTrainingListItem(training));
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 15. Activate / De-Activate Trainee (PATCH) — non-idempotent.
     */
    @ApiOperation(
            value = "Activate / De-Activate Trainee",
            notes = "Sets trainee active status. Fails if already in the requested state. Requires authentication."
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Status updated"),
            @ApiResponse(code = 400, message = "Invalid request or already in that state"),
            @ApiResponse(code = 401, message = "Unauthorized"),
            @ApiResponse(code = 404, message = "Trainee not found")
    })
    @PatchMapping("/{username}")
    public ResponseEntity<Void> setActive(
            @ApiParam(value = "Trainee username", required = true) @PathVariable("username") String username,
            @ApiParam(value = "Password (authentication)", required = true) @RequestParam("password") String password,
            @RequestBody ActivateRequest request) {

        log.info("PATCH /trainees/{} isActive={}", username, request != null ? request.getIsActive() : null);

        RequestValidation.requireNonNull(request, "Request body");
        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);
        if (!gymFacade.matchTraineeCredentials(user, pass)) {
            log.warn("Unauthorized activate/deactivate for trainee username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RequestValidation.requireSameUsername(user, request.getUsername());
        RequestValidation.requireNonNull(request.getIsActive(), "Is Active");

        Trainee trainee = gymFacade.selectTraineeByUsername(user);
        gymFacade.setTraineeActive(trainee.getId(), request.getIsActive());
        log.info("Trainee {} isActive set to {}", user, request.getIsActive());
        return ResponseEntity.ok().build();
    }

    private TrainingListItemDto toTrainingListItem(Training training) {
        String typeName = training.getTrainingType() != null
                ? training.getTrainingType().getTrainingTypeName()
                : null;
        String trainerName = null;
        if (training.getTrainerId() != null) {
            Trainer trainer = gymFacade.selectTrainer(training.getTrainerId());
            if (trainer.getUser() != null) {
                trainerName = (trainer.getUser().getFirstName() + " " + trainer.getUser().getLastName()).trim();
            }
        }
        return new TrainingListItemDto(
                training.getTrainingName(),
                training.getTrainingDate(),
                typeName,
                training.getTrainingDuration(),
                trainerName);
    }

    private TraineeProfileResponse toProfileResponse(Trainee trainee) {
        User user = trainee.getUser();
        TraineeProfileResponse response = new TraineeProfileResponse();
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setDateOfBirth(trainee.getDateOfBirth());
        response.setAddress(trainee.getAddress());
        response.setIsActive(user.isActive());
        response.setTrainersList(toTrainersList(trainee));
        return response;
    }

    private List<TrainerShortDto> toTrainersList(Trainee trainee) {
        List<TrainerShortDto> trainers = new ArrayList<>();
        if (trainee.getTrainerIds() == null) {
            return trainers;
        }
        for (Long trainerId : trainee.getTrainerIds()) {
            Trainer trainer = gymFacade.selectTrainer(trainerId);
            User trainerUser = trainer.getUser();
            String specialization = trainer.getSpecialization() != null
                    ? trainer.getSpecialization().getTrainingTypeName()
                    : null;
            trainers.add(new TrainerShortDto(
                    trainerUser.getUsername(),
                    trainerUser.getFirstName(),
                    trainerUser.getLastName(),
                    specialization));
        }
        return trainers;
    }
}
