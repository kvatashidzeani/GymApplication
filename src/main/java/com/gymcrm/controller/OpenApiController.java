package com.gymcrm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Serves OpenAPI JSON so Swagger UI can list and try endpoints.
 */
@RestController
public class OpenApiController {

    @GetMapping(value = "/v3/api-docs", produces = "application/json")
    public Map<String, Object> apiDocs() {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("openapi", "3.0.1");
        doc.put("info", Map.of(
                "title", "Gym CRM REST API",
                "version", "1.0",
                "description", "Gym CRM REST endpoints"
        ));

        Map<String, Object> paths = new LinkedHashMap<>();
        paths.put("/trainees/register", Map.of("post", traineeRegisterOperation()));
        paths.put("/trainees/{username}", Map.of(
                "get", getTraineeProfileOperation(),
                "put", updateTraineeProfileOperation(),
                "delete", deleteTraineeProfileOperation(),
                "patch", activateTraineeOperation()
        ));
        paths.put("/trainees/{username}/trainers/not-assigned", Map.of(
                "get", getNotAssignedActiveTrainersOperation()
        ));
        paths.put("/trainees/{username}/trainers", Map.of(
                "put", updateTraineeTrainersListOperation()
        ));
        paths.put("/trainees/{username}/trainings", Map.of(
                "get", getTraineeTrainingsOperation()
        ));
        paths.put("/trainers/register", Map.of("post", trainerRegisterOperation()));
        paths.put("/trainers/{username}", Map.of(
                "get", getTrainerProfileOperation(),
                "put", updateTrainerProfileOperation(),
                "patch", activateTrainerOperation()
        ));
        paths.put("/trainers/{username}/trainings", Map.of(
                "get", getTrainerTrainingsOperation()
        ));
        paths.put("/trainings", Map.of(
                "post", addTrainingOperation()
        ));
        paths.put("/training-types", Map.of(
                "get", getTrainingTypesOperation()
        ));
        paths.put("/login", Map.of(
                "get", loginOperation(),
                "put", changeLoginOperation()
        ));
        doc.put("paths", paths);

        Map<String, Object> schemas = new LinkedHashMap<>();
        schemas.put("TraineeRegistrationRequest", Map.of(
                "type", "object",
                "required", List.of("firstName", "lastName"),
                "properties", Map.of(
                        "firstName", Map.of("type", "string", "example", "Ani"),
                        "lastName", Map.of("type", "string", "example", "Kvatashidze"),
                        "dateOfBirth", Map.of("type", "string", "format", "date", "example", "2005-06-09"),
                        "address", Map.of("type", "string", "example", "Gora")
                )
        ));
        schemas.put("TrainerRegistrationRequest", Map.of(
                "type", "object",
                "required", List.of("firstName", "lastName", "specialization"),
                "properties", Map.of(
                        "firstName", Map.of("type", "string", "example", "Giorgi"),
                        "lastName", Map.of("type", "string", "example", "Janelidze"),
                        "specialization", Map.of(
                                "type", "string",
                                "example", "Cardio",
                                "description", "Existing training type name"
                        )
                )
        ));
        schemas.put("RegistrationResponse", Map.of(
                "type", "object",
                "properties", Map.of(
                        "username", Map.of("type", "string", "example", "Ani.Kvatashidze"),
                        "password", Map.of("type", "string", "example", "aB3dE6gH")
                )
        ));
        schemas.put("ChangeLoginRequest", Map.of(
                "type", "object",
                "required", List.of("username", "oldPassword", "newPassword"),
                "properties", Map.of(
                        "username", Map.of("type", "string", "example", "Ani.Kvatashidze"),
                        "oldPassword", Map.of("type", "string", "example", "aB3dE6gH"),
                        "newPassword", Map.of("type", "string", "example", "newPass123")
                )
        ));
        schemas.put("TrainerShortDto", Map.of(
                "type", "object",
                "properties", Map.of(
                        "username", Map.of("type", "string", "example", "Mike.Brown"),
                        "firstName", Map.of("type", "string", "example", "Mike"),
                        "lastName", Map.of("type", "string", "example", "Brown"),
                        "specialization", Map.of("type", "string", "example", "Cardio")
                )
        ));
        schemas.put("TraineeProfileResponse", Map.of(
                "type", "object",
                "properties", Map.of(
                        "username", Map.of("type", "string", "example", "Ani.Kvatashidze"),
                        "firstName", Map.of("type", "string", "example", "Ani"),
                        "lastName", Map.of("type", "string", "example", "Kvatashidze"),
                        "dateOfBirth", Map.of("type", "string", "format", "date", "example", "2005-06-09"),
                        "address", Map.of("type", "string", "example", "Gora"),
                        "isActive", Map.of("type", "boolean", "example", true),
                        "trainersList", Map.of(
                                "type", "array",
                                "items", Map.of("$ref", "#/components/schemas/TrainerShortDto")
                        )
                )
        ));
        schemas.put("UpdateTraineeProfileRequest", Map.of(
                "type", "object",
                "required", List.of("username", "firstName", "lastName", "isActive"),
                "properties", Map.of(
                        "username", Map.of("type", "string", "example", "Ani.Kvatashidze"),
                        "firstName", Map.of("type", "string", "example", "Ani"),
                        "lastName", Map.of("type", "string", "example", "Kvatashidze"),
                        "dateOfBirth", Map.of("type", "string", "format", "date", "example", "2005-06-09"),
                        "address", Map.of("type", "string", "example", "Vake"),
                        "isActive", Map.of("type", "boolean", "example", true)
                )
        ));
        schemas.put("TrainerUsernameDto", Map.of(
                "type", "object",
                "required", List.of("username"),
                "properties", Map.of(
                        "username", Map.of("type", "string", "example", "Mike.Brown")
                )
        ));
        schemas.put("UpdateTraineeTrainersRequest", Map.of(
                "type", "object",
                "required", List.of("traineeUsername", "trainersList"),
                "properties", Map.of(
                        "traineeUsername", Map.of("type", "string", "example", "Ani.Smith"),
                        "trainersList", Map.of(
                                "type", "array",
                                "items", Map.of("$ref", "#/components/schemas/TrainerUsernameDto")
                        )
                )
        ));
        schemas.put("TraineeShortDto", Map.of(
                "type", "object",
                "properties", Map.of(
                        "username", Map.of("type", "string", "example", "Ani.Smith"),
                        "firstName", Map.of("type", "string", "example", "Ani"),
                        "lastName", Map.of("type", "string", "example", "Smith")
                )
        ));
        schemas.put("TrainerProfileResponse", Map.of(
                "type", "object",
                "properties", Map.of(
                        "username", Map.of("type", "string", "example", "Mike.Brown"),
                        "firstName", Map.of("type", "string", "example", "Mike"),
                        "lastName", Map.of("type", "string", "example", "Brown"),
                        "specialization", Map.of("type", "string", "example", "Cardio"),
                        "isActive", Map.of("type", "boolean", "example", true),
                        "traineesList", Map.of(
                                "type", "array",
                                "items", Map.of("$ref", "#/components/schemas/TraineeShortDto")
                        )
                )
        ));
        schemas.put("UpdateTrainerProfileRequest", Map.of(
                "type", "object",
                "required", List.of("username", "firstName", "lastName", "isActive"),
                "properties", Map.of(
                        "username", Map.of("type", "string", "example", "Mike.Brown"),
                        "firstName", Map.of("type", "string", "example", "Mike"),
                        "lastName", Map.of("type", "string", "example", "Brown"),
                        "specialization", Map.of(
                                "type", "string",
                                "example", "Cardio",
                                "description", "Read-only; ignored on update"
                        ),
                        "isActive", Map.of("type", "boolean", "example", true)
                )
        ));
        schemas.put("TrainingListItemDto", Map.of(
                "type", "object",
                "properties", Map.of(
                        "trainingName", Map.of("type", "string", "example", "Morning Cardio"),
                        "trainingDate", Map.of("type", "string", "format", "date", "example", "2024-11-10"),
                        "trainingType", Map.of("type", "string", "example", "Cardio"),
                        "trainingDuration", Map.of("type", "integer", "example", 45),
                        "trainerName", Map.of("type", "string", "example", "Mike Brown")
                )
        ));
        schemas.put("TrainerTrainingListItemDto", Map.of(
                "type", "object",
                "properties", Map.of(
                        "trainingName", Map.of("type", "string", "example", "Morning Cardio"),
                        "trainingDate", Map.of("type", "string", "format", "date", "example", "2024-11-10"),
                        "trainingType", Map.of("type", "string", "example", "Cardio"),
                        "trainingDuration", Map.of("type", "integer", "example", 45),
                        "traineeName", Map.of("type", "string", "example", "John Doe")
                )
        ));
        schemas.put("AddTrainingRequest", Map.of(
                "type", "object",
                "required", List.of(
                        "traineeUsername", "trainerUsername", "trainingName", "trainingDate", "trainingDuration"
                ),
                "properties", Map.of(
                        "traineeUsername", Map.of("type", "string", "example", "John.Doe"),
                        "trainerUsername", Map.of("type", "string", "example", "Mike.Brown"),
                        "trainingName", Map.of("type", "string", "example", "Morning Cardio"),
                        "trainingDate", Map.of("type", "string", "format", "date", "example", "2024-11-20"),
                        "trainingDuration", Map.of("type", "integer", "example", 45)
                )
        ));
        schemas.put("ActivateRequest", Map.of(
                "type", "object",
                "required", List.of("username", "isActive"),
                "properties", Map.of(
                        "username", Map.of("type", "string", "example", "John.Doe"),
                        "isActive", Map.of("type", "boolean", "example", true)
                )
        ));
        schemas.put("TrainingTypeDto", Map.of(
                "type", "object",
                "properties", Map.of(
                        "trainingType", Map.of("type", "string", "example", "Cardio"),
                        "trainingTypeId", Map.of("type", "integer", "example", 1)
                )
        ));
        doc.put("components", Map.of("schemas", schemas));
        return doc;
    }

    private static Map<String, Object> traineeRegisterOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainee"));
        op.put("summary", "Trainee Registration");
        op.put("description",
                "Registers a trainee. First name and last name are required. "
                        + "Date of birth and address are optional. "
                        + "Username (FirstName.LastName[+serial]) and password are generated automatically. "
                        + "Cannot register if already a trainer with the same first and last name. "
                        + "Returns generated username and password.");
        op.put("requestBody", Map.of(
                "required", true,
                "content", Map.of(
                        "application/json", Map.of(
                                "schema", Map.of("$ref", "#/components/schemas/TraineeRegistrationRequest")
                        )
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "Registration successful",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of("$ref", "#/components/schemas/RegistrationResponse")
                                )
                        )
                ),
                "400", Map.of("description", "Invalid request")
        ));
        return op;
    }

    private static Map<String, Object> getTraineeProfileOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainee"));
        op.put("summary", "Get Trainee Profile");
        op.put("description",
                "Returns trainee profile and assigned trainers. "
                        + "Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "Ani.Kvatashidze"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "Profile found",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of("$ref", "#/components/schemas/TraineeProfileResponse")
                                )
                        )
                ),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainee not found")
        ));
        return op;
    }

    private static Map<String, Object> updateTraineeProfileOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainee"));
        op.put("summary", "Update Trainee Profile");
        op.put("description",
                "Updates trainee profile. Username cannot be changed. "
                        + "Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "Ani.Kvatashidze"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("requestBody", Map.of(
                "required", true,
                "content", Map.of(
                        "application/json", Map.of(
                                "schema", Map.of("$ref", "#/components/schemas/UpdateTraineeProfileRequest")
                        )
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "Profile updated",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of("$ref", "#/components/schemas/TraineeProfileResponse")
                                )
                        )
                ),
                "400", Map.of("description", "Invalid request"),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainee not found")
        ));
        return op;
    }

    private static Map<String, Object> deleteTraineeProfileOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainee"));
        op.put("summary", "Delete Trainee Profile");
        op.put("description",
                "Hard-deletes trainee profile, linked user, and related trainings. "
                        + "Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "Ani.Kvatashidze"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of("description", "Trainee deleted"),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainee not found")
        ));
        return op;
    }

    private static Map<String, Object> activateTraineeOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainee"));
        op.put("summary", "Activate / De-Activate Trainee");
        op.put("description",
                "Sets trainee active status (non-idempotent: fails if already in that state). "
                        + "Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "John.Doe"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("requestBody", Map.of(
                "required", true,
                "content", Map.of(
                        "application/json", Map.of(
                                "schema", Map.of("$ref", "#/components/schemas/ActivateRequest")
                        )
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of("description", "Status updated"),
                "400", Map.of("description", "Invalid request or already in that state"),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainee not found")
        ));
        return op;
    }

    private static Map<String, Object> getNotAssignedActiveTrainersOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainee"));
        op.put("summary", "Get not assigned active trainers");
        op.put("description",
                "Returns active trainers that are not assigned to the trainee. "
                        + "Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "Ani.Smith"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "List of active unassigned trainers",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of(
                                                "type", "array",
                                                "items", Map.of("$ref", "#/components/schemas/TrainerShortDto")
                                        )
                                )
                        )
                ),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainee not found")
        ));
        return op;
    }

    private static Map<String, Object> updateTraineeTrainersListOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainee"));
        op.put("summary", "Update Trainee's Trainer List");
        op.put("description",
                "Replaces the trainee's assigned trainers list. "
                        + "Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "Ani.Smith"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("requestBody", Map.of(
                "required", true,
                "content", Map.of(
                        "application/json", Map.of(
                                "schema", Map.of("$ref", "#/components/schemas/UpdateTraineeTrainersRequest")
                        )
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "Updated trainers list",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of(
                                                "type", "array",
                                                "items", Map.of("$ref", "#/components/schemas/TrainerShortDto")
                                        )
                                )
                        )
                ),
                "400", Map.of("description", "Invalid request"),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainee or trainer not found")
        ));
        return op;
    }

    private static Map<String, Object> getTraineeTrainingsOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainee"));
        op.put("summary", "Get Trainee Trainings List");
        op.put("description",
                "Returns trainings for the trainee. Optional filters: periodFrom, periodTo, "
                        + "trainerName, trainingType. Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "John.Doe"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                ),
                Map.of(
                        "name", "periodFrom",
                        "in", "query",
                        "required", false,
                        "schema", Map.of("type", "string", "format", "date"),
                        "example", "2024-11-01"
                ),
                Map.of(
                        "name", "periodTo",
                        "in", "query",
                        "required", false,
                        "schema", Map.of("type", "string", "format", "date"),
                        "example", "2024-11-30"
                ),
                Map.of(
                        "name", "trainerName",
                        "in", "query",
                        "required", false,
                        "schema", Map.of("type", "string"),
                        "example", "Mike Brown"
                ),
                Map.of(
                        "name", "trainingType",
                        "in", "query",
                        "required", false,
                        "schema", Map.of("type", "string"),
                        "example", "Cardio"
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "Trainings list",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of(
                                                "type", "array",
                                                "items", Map.of("$ref", "#/components/schemas/TrainingListItemDto")
                                        )
                                )
                        )
                ),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainee not found")
        ));
        return op;
    }

    private static Map<String, Object> trainerRegisterOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainer"));
        op.put("summary", "Trainer Registration");
        op.put("description",
                "Registers a trainer. First name, last name, and specialization (training type name) "
                        + "are required. Username (FirstName.LastName[+serial]) and password are generated automatically. "
                        + "Cannot register if already a trainee with the same first and last name. "
                        + "Returns generated username and password.");
        op.put("requestBody", Map.of(
                "required", true,
                "content", Map.of(
                        "application/json", Map.of(
                                "schema", Map.of("$ref", "#/components/schemas/TrainerRegistrationRequest")
                        )
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "Registration successful",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of("$ref", "#/components/schemas/RegistrationResponse")
                                )
                        )
                ),
                "400", Map.of("description", "Invalid request or unknown training type")
        ));
        return op;
    }

    private static Map<String, Object> getTrainerProfileOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainer"));
        op.put("summary", "Get Trainer Profile");
        op.put("description",
                "Returns trainer profile and assigned trainees. "
                        + "Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "Mike.Brown"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "Profile found",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of("$ref", "#/components/schemas/TrainerProfileResponse")
                                )
                        )
                ),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainer not found")
        ));
        return op;
    }

    private static Map<String, Object> updateTrainerProfileOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainer"));
        op.put("summary", "Update Trainer Profile");
        op.put("description",
                "Updates trainer profile. Username and specialization are read-only. "
                        + "Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "Mike.Brown"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("requestBody", Map.of(
                "required", true,
                "content", Map.of(
                        "application/json", Map.of(
                                "schema", Map.of("$ref", "#/components/schemas/UpdateTrainerProfileRequest")
                        )
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "Profile updated",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of("$ref", "#/components/schemas/TrainerProfileResponse")
                                )
                        )
                ),
                "400", Map.of("description", "Invalid request"),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainer not found")
        ));
        return op;
    }

    private static Map<String, Object> activateTrainerOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainer"));
        op.put("summary", "Activate / De-Activate Trainer");
        op.put("description",
                "Sets trainer active status (non-idempotent: fails if already in that state). "
                        + "Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "Mike.Brown"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("requestBody", Map.of(
                "required", true,
                "content", Map.of(
                        "application/json", Map.of(
                                "schema", Map.of("$ref", "#/components/schemas/ActivateRequest")
                        )
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of("description", "Status updated"),
                "400", Map.of("description", "Invalid request or already in that state"),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainer not found")
        ));
        return op;
    }

    private static Map<String, Object> getTrainerTrainingsOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Trainer"));
        op.put("summary", "Get Trainer Trainings List");
        op.put("description",
                "Returns trainings for the trainer. Optional filters: periodFrom, periodTo, traineeName. "
                        + "Password query param is required for authentication.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "path",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "Mike.Brown"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                ),
                Map.of(
                        "name", "periodFrom",
                        "in", "query",
                        "required", false,
                        "schema", Map.of("type", "string", "format", "date"),
                        "example", "2024-11-01"
                ),
                Map.of(
                        "name", "periodTo",
                        "in", "query",
                        "required", false,
                        "schema", Map.of("type", "string", "format", "date"),
                        "example", "2024-11-30"
                ),
                Map.of(
                        "name", "traineeName",
                        "in", "query",
                        "required", false,
                        "schema", Map.of("type", "string"),
                        "example", "John Doe"
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "Trainings list",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of(
                                                "type", "array",
                                                "items", Map.of("$ref", "#/components/schemas/TrainerTrainingListItemDto")
                                        )
                                )
                        )
                ),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainer not found")
        ));
        return op;
    }

    private static Map<String, Object> addTrainingOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Training"));
        op.put("summary", "Add Training");
        op.put("description",
                "Creates a training session. Training type is taken from the trainer's specialization. "
                        + "Requires username and password of the trainee or trainer involved.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "John.Doe"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("requestBody", Map.of(
                "required", true,
                "content", Map.of(
                        "application/json", Map.of(
                                "schema", Map.of("$ref", "#/components/schemas/AddTrainingRequest")
                        )
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of("description", "Training created"),
                "400", Map.of("description", "Invalid request"),
                "401", Map.of("description", "Unauthorized"),
                "404", Map.of("description", "Trainee or trainer not found")
        ));
        return op;
    }

    private static Map<String, Object> getTrainingTypesOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Training Type"));
        op.put("summary", "Get Training types");
        op.put("description",
                "Returns all available training types. "
                        + "Requires username and password (trainee or trainer authentication).");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "John.Doe"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of(
                        "description", "Training types list",
                        "content", Map.of(
                                "application/json", Map.of(
                                        "schema", Map.of(
                                                "type", "array",
                                                "items", Map.of("$ref", "#/components/schemas/TrainingTypeDto")
                                        )
                                )
                        )
                ),
                "401", Map.of("description", "Unauthorized")
        ));
        return op;
    }

    private static Map<String, Object> loginOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Auth"));
        op.put("summary", "Login");
        op.put("description", "Validates username and password for a trainee or trainer. Returns 200 OK if valid.");
        op.put("parameters", List.of(
                Map.of(
                        "name", "username",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "Ani.Kvatashidze"
                ),
                Map.of(
                        "name", "password",
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string"),
                        "example", "aB3dE6gH"
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of("description", "Credentials valid"),
                "400", Map.of("description", "Missing username or password"),
                "401", Map.of("description", "Invalid credentials")
        ));
        return op;
    }

    private static Map<String, Object> changeLoginOperation() {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("tags", List.of("Auth"));
        op.put("summary", "Change Login");
        op.put("description", "Changes password after verifying username and old password.");
        op.put("requestBody", Map.of(
                "required", true,
                "content", Map.of(
                        "application/json", Map.of(
                                "schema", Map.of("$ref", "#/components/schemas/ChangeLoginRequest")
                        )
                )
        ));
        op.put("responses", Map.of(
                "200", Map.of("description", "Password changed"),
                "400", Map.of("description", "Missing required fields"),
                "401", Map.of("description", "Invalid username or old password")
        ));
        return op;
    }
}
