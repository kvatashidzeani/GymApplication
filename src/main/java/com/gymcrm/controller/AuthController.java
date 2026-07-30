package com.gymcrm.controller;

import com.gymcrm.dto.ChangeLoginRequest;
import com.gymcrm.facade.GymFacade;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Auth")
@RestController
@RequestMapping
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final GymFacade gymFacade;

    public AuthController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    /**
     * 3. Login (GET)
     * Request: username, password (query params, required)
     * Response: 200 OK if credentials match a trainee or trainer
     */
    @ApiOperation(value = "Login", notes = "Checks username and password for trainee or trainer.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Credentials valid"),
            @ApiResponse(code = 400, message = "Missing username or password"),
            @ApiResponse(code = 401, message = "Invalid credentials")
    })
    @GetMapping("/login")
    public ResponseEntity<Void> login(
            @ApiParam(value = "Username", required = true) @RequestParam("username") String username,
            @ApiParam(value = "Password", required = true) @RequestParam("password") String password) {

        log.info("GET /login username={}", username);

        String user = RequestValidation.requireUsername(username);
        String pass = RequestValidation.requirePassword(password);

        boolean ok = gymFacade.matchTraineeCredentials(user, pass)
                || gymFacade.matchTrainerCredentials(user, pass);

        if (!ok) {
            log.warn("Login failed for username={}", user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Login successful for username={}", user);
        return ResponseEntity.ok().build();
    }

    /**
     * 4. Change Login (PUT)
     * Request: username, oldPassword, newPassword (required)
     * Response: 200 OK
     */
    @ApiOperation(value = "Change Login", notes = "Changes password for a trainee or trainer after verifying the old password.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password changed"),
            @ApiResponse(code = 400, message = "Missing required fields"),
            @ApiResponse(code = 401, message = "Invalid username or old password")
    })
    @PutMapping("/login")
    public ResponseEntity<Void> changeLogin(@RequestBody ChangeLoginRequest request) {
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Password changed successfully for username={}", username);
        return ResponseEntity.ok().build();
    }
}
