package com.gymcrm.controller;

import com.gymcrm.actuator.metrics.GymMetrics;
import com.gymcrm.dto.ChangeLoginRequest;
import com.gymcrm.dto.ErrorResponse;
import com.gymcrm.exceptions.UnauthorizedException;
import com.gymcrm.facade.GymFacade;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Auth API", tags = "Auth", description = "Authentication and password management operations")
@RestController
@RequestMapping
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final GymFacade gymFacade;
    private final GymMetrics gymMetrics;

    public AuthController(GymFacade gymFacade, GymMetrics gymMetrics) {
        this.gymFacade = gymFacade;
        this.gymMetrics = gymMetrics;
    }

    /**
     * 3. Login (GET)
     * Request: username, password (query params, required)
     * Response: 200 OK if credentials match a trainee or trainer
     */
    @ApiOperation(value = "Login", notes = "Checks username and password for trainee or trainer.", response = Void.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Credentials valid"),
            @ApiResponse(code = 400, message = "Missing username or password", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Invalid credentials", response = ErrorResponse.class)
    })
    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @ApiParam(value = "Username", required = true) @RequestParam("username") String username,
            @ApiParam(value = "Password", required = true) @RequestParam("password") String password) {

        log.info("GET /login username={}", username);

        Timer.Sample sample = gymMetrics.startLoginTimer();
        try {
            String user = RequestValidation.requireUsername(username);
            String pass = RequestValidation.requirePassword(password);

            boolean ok = gymFacade.matchTraineeCredentials(user, pass)
                    || gymFacade.matchTrainerCredentials(user, pass);

            if (!ok) {
                gymMetrics.loginFailed();
                log.warn("Login failed for username={}", user);
                throw new UnauthorizedException("Invalid credentials");
            }

            gymMetrics.loginSucceeded();
            log.info("Login successful for username={}", user);
            return ResponseEntity.ok().build();
        } finally {
            gymMetrics.stopLoginTimer(sample);
        }
    }

    /**
     * 4. Change Login (PUT)
     * Request: username, oldPassword, newPassword (required)
     * Response: 200 OK
     */
    @ApiOperation(value = "Change Login", notes = "Changes password for a trainee or trainer after verifying the old password.", response = Void.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password changed"),
            @ApiResponse(code = 400, message = "Missing required fields", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Invalid username or old password", response = ErrorResponse.class)
    })
    @PutMapping("/login")
    public ResponseEntity<Void> changeLogin(
            @ApiParam(value = "Change login request with username, old password, and new password", required = true)
            @RequestBody ChangeLoginRequest request) {
        RequestValidation.requireNonNull(request, "Request body");
        log.info("PUT /login username={}", request.getUsername());

        String username = RequestValidation.requireUsername(request.getUsername());
        String oldPassword = RequestValidation.requireNonBlank(request.getOldPassword(), "Old password");
        String newPassword = RequestValidation.requireNonBlank(request.getNewPassword(), "New password");

        if (gymFacade.matchTraineeCredentials(username, oldPassword)) {
            gymFacade.changeTraineePassword(username, oldPassword, newPassword);
        } else if (gymFacade.matchTrainerCredentials(username, oldPassword)) {
            gymFacade.changeTrainerPassword(username, oldPassword, newPassword);
        } else {
            log.warn("Change login failed for username={}", username);
            throw new UnauthorizedException("Unauthorized");
        }

        log.info("Password changed successfully for username={}", username);
        return ResponseEntity.ok().build();
    }
}
