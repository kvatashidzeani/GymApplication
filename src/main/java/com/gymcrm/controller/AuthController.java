package com.gymcrm.controller;

import com.gymcrm.actuator.metrics.GymMetrics;
import com.gymcrm.dto.ChangeLoginRequest;
import com.gymcrm.dto.ErrorResponse;
import com.gymcrm.dto.JwtResponse;
import com.gymcrm.exceptions.AccountLockedException;
import com.gymcrm.exceptions.UnauthorizedException;
import com.gymcrm.facade.GymFacade;
import com.gymcrm.security.JwtService;
import com.gymcrm.security.LoginAttemptService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    private static final String LOCKED_MESSAGE =
            "User is blocked for " + LoginAttemptService.LOCK_DURATION_MINUTES
                    + " minutes due to too many failed login attempts";

    private final GymFacade gymFacade;
    private final GymMetrics gymMetrics;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final JwtService jwtService;

    public AuthController(GymFacade gymFacade, GymMetrics gymMetrics,
                          AuthenticationManager authenticationManager,
                          LoginAttemptService loginAttemptService,
                          JwtService jwtService) {
        this.gymFacade = gymFacade;
        this.gymMetrics = gymMetrics;
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
        this.jwtService = jwtService;
    }

    /**
     * Login — authenticates credentials and returns a JWT Bearer token.
     */
    @ApiOperation(
            value = "Login",
            notes = "Authenticates with Spring Security and returns a JWT. Use Authorization: Bearer <token> on protected endpoints.",
            response = JwtResponse.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Credentials valid; JWT returned", response = JwtResponse.class),
            @ApiResponse(code = 400, message = "Missing username or password", response = ErrorResponse.class),
            @ApiResponse(code = 401, message = "Invalid credentials", response = ErrorResponse.class),
            @ApiResponse(code = 429, message = "User temporarily blocked (brute-force protection)", response = ErrorResponse.class)
    })
    @GetMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @ApiParam(value = "Username", required = true) @RequestParam String username,
            @ApiParam(value = "Password", required = true) @RequestParam String password) {
        Timer.Sample sample = gymMetrics.startLoginTimer();
        try {
            String user = RequestValidation.requireUsername(username);
            String pass = RequestValidation.requirePassword(password);
            log.info("GET /login username={}", user);

            if (loginAttemptService.isBlocked(user)) {
                gymMetrics.loginFailed();
                log.warn("Login rejected: username={} is temporarily blocked", user);
                throw new AccountLockedException(LOCKED_MESSAGE);
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user, pass));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token;
            if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                token = jwtService.generateToken(userDetails);
            } else {
                token = jwtService.generateToken(user);
            }

            loginAttemptService.loginSucceeded(user);
            gymMetrics.loginSucceeded();
            log.info("Login successful for username={} (JWT issued)", user);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (AccountLockedException ex) {
            throw ex;
        } catch (LockedException ex) {
            gymMetrics.loginFailed();
            log.warn("Login blocked: {}", ex.getMessage());
            throw new AccountLockedException(LOCKED_MESSAGE);
        } catch (AuthenticationException ex) {
            String user = username != null ? username.trim() : null;
            if (user != null && !user.isEmpty()) {
                loginAttemptService.loginFailed(user);
            }
            gymMetrics.loginFailed();
            log.warn("Login failed: {}", ex.getMessage());
            throw new UnauthorizedException("Unauthorized");
        } finally {
            gymMetrics.stopLoginTimer(sample);
        }
    }

    /**
     * Change Login — requires JWT Bearer auth; body carries old/new password.
     */
    @ApiOperation(value = "Change Login", notes = "Changes password after verifying the old password. Requires JWT Bearer auth.", response = Void.class)
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
        String authenticated = SecurityUtils.currentUsername();
        String username = RequestValidation.requireUsername(request.getUsername());
        SecurityUtils.requireSelf(username);

        log.info("PUT /login username={}", username);

        String oldPassword = RequestValidation.requireNonBlank(request.getOldPassword(), "Old password");
        String newPassword = RequestValidation.requireNonBlank(request.getNewPassword(), "New password");

        if (gymFacade.matchTraineeCredentials(username, oldPassword)) {
            gymFacade.changeTraineePassword(username, oldPassword, newPassword);
        } else if (gymFacade.matchTrainerCredentials(username, oldPassword)) {
            gymFacade.changeTrainerPassword(username, oldPassword, newPassword);
        } else {
            log.warn("Change login failed for username={} (authenticated as {})", username, authenticated);
            throw new UnauthorizedException("Unauthorized");
        }

        log.info("Password changed successfully for username={}", username);
        return ResponseEntity.ok().build();
    }
}
