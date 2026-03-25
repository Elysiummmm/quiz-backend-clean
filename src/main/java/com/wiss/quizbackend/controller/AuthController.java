package com.wiss.quizbackend.controller;

import com.wiss.quizbackend.dto.LoginRequestDTO;
import com.wiss.quizbackend.dto.LoginResponseDTO;
import com.wiss.quizbackend.dto.RegisterRequestDTO;
import com.wiss.quizbackend.dto.RegisterResponseDTO;
import com.wiss.quizbackend.entity.AppUser;
import com.wiss.quizbackend.entity.Role;
import com.wiss.quizbackend.service.AppUserService;
import com.wiss.quizbackend.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // React Frontend
public class AuthController {
    private final AppUserService appUserService;
    private final JwtService jwtService;

    public AuthController(AppUserService appUserService, JwtService jwtService) {
        this.appUserService = appUserService;
        this.jwtService = jwtService;
    }

    /**
     * POST /api/auth/register
     *<br>
     * Request Body Example:
     * <code>{
     *   "username": "maxmuster",
     *   "email": "max@example.com",
     *   "password": "secure123"
     * }</code>
     *<br>
     * Success Response (200):
     * <code>{
     *   "id": 1,
     *   "username": "maxmuster",
     *   "email": "max@example.com",
     *   "role": "PLAYER",
     *   "message": "Registrierung erfolgreich! Willkommen maxmuster!"
     * }</code>
     *<br>
     * Error Response (400):
     * <code>{
     *   "error": "Username 'maxmuster' ist bereits vergeben"
     * }</code>
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody
                                      RegisterRequestDTO request) {
        try {
            String email = request.getEmail();
            String rawPassword = request.getPassword();
            String username = request.getUsername();

            if (!appUserService.isValidEmail(email)) { throw new IllegalArgumentException("No valid E-Mail provided"); }
            if (!appUserService.isEmailAvailable(email)) { throw new IllegalArgumentException("E-Mail unavailable"); }
            if (!appUserService.isUsernameAvailable(username)) { throw new IllegalArgumentException("Username unavailable"); }

            AppUser user = appUserService.registerUser(username, email, rawPassword, Role.Player);

            RegisterResponseDTO res = new RegisterResponseDTO(user.getId(), user.getUsername(), user.getEmail(), "Player");

            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST /api/auth/login
     *
     * Authentifiziert einen User und gibt JWT Token zurück.
     *
     * Request Body Example:
     * {
     *   "usernameOrEmail": "maxmuster",
     *   "password": "test123"
     * }
     *
     * Success Response (200):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "tokenType": "Bearer",
     *   "userId": 1,
     *   "username": "maxmuster",
     *   "email": "max@example.com",
     *   "role": "PLAYER",
     *   "expiresIn": 86400000
     * }
     *
     * Error Response (401):
     * {
     *   "error": "Ungültige Anmeldedaten"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            Optional<AppUser> userOpt;

            if (request.getUsernameOrEmail().contains("@")) {
                userOpt = appUserService
                        .findByEmail(request.getUsernameOrEmail());
            } else {
                userOpt = appUserService
                        .findByUsername(request.getUsernameOrEmail());
            }

            if (userOpt.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ungültige Anmeldedaten"));
            }

            AppUser user = userOpt.get();

            Optional<AppUser> authenticatedUser =
                    appUserService.authenticateUser(user.getUsername(),
                            request.getPassword());

            if (authenticatedUser.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Ungültige Anmeldedaten"));
            }

            String token = jwtService.generateToken(
                    user.getUsername(),
                    user.getRole().name()
            );

            LoginResponseDTO response = new LoginResponseDTO(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().name(),
                    86400000L
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error",
                            "Ein Fehler ist aufgetreten: " + e.getMessage()));
        }
    }

    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Object>> checkUsername(@PathVariable String username) {
        boolean isAvailable = appUserService.isUsernameAvailable(username);
        String message = isAvailable ? "Username ist verfügbar" : "Username bereits registriert";

        return new ResponseEntity<>(Map.of(
                "available", isAvailable,
                "message", message
        ), HttpStatus.OK);
    }

    /**
     * GET /api/auth/check-email/{email}
     *<br>
     * Prüft ob eine Email verfügbar ist.
     *<br>
     * Response:
     * <code>{
     *   "available": true/false,
     *   "message": "Email ist verfügbar" / "Email bereits registriert"
     * }</code>
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Object>> checkEmail(@PathVariable String email) {
        boolean isAvailable = appUserService.isEmailAvailable(email);
        String message = isAvailable ? "E-Mail ist verfügbar" : "E-Mail bereits registriert";

        return new ResponseEntity<>(Map.of(
                "available", isAvailable,
                "message", message
        ), HttpStatus.OK);
    }

    /**
     * GET /api/auth/test
     *<br>
     * Simple Test Endpoint.
     * Prüft ob der Controller erreichbar ist.
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}