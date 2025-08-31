package com.brandmast.api.controllers.auth;

import com.brandmast.api.controllers.auth.dto.LoginRequest;
import com.brandmast.api.entity.User;
import com.brandmast.api.repository.AdminRepository;
import com.brandmast.api.repository.BrandmasterRepository;
import com.brandmast.api.repository.SupervisorRepository;
import com.brandmast.api.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private String getRole(User user) {
        boolean isBrandmaster = brandmasterRepository.findByUser_IdUser(user.getIdUser()).isPresent();
        boolean isSupervisor = supervisorRepository.findByUser_IdUser(user.getIdUser()).isPresent();
        boolean isAdmin = adminRepository.findByUser_IdUser(user.getIdUser()).isPresent();

        String role = "UNAUTHORIZED";
        if (isBrandmaster) {
            role = "BM";
        }
        if (isSupervisor) {
            role = "SV";
        }
        if (isAdmin) {
            role = "ADMIN";
        }

        return role;
    }

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BrandmasterRepository brandmasterRepository;
    @Autowired
    private SupervisorRepository supervisorRepository;
    @Autowired
    private AdminRepository adminRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        System.out.println("(+)[REQUEST_LOG] at /api/auth/login POST Received login request: " + loginRequest.getLogin());

        //Zdobywanie loginu z request, automatycznie przypisywanie znalezionego uzytkoniwk do zmiennej
        Optional<User> user = userRepository.findByLogin(loginRequest.getLogin());

        if (user.isPresent()) {
            User user_from_request = user.get();

            String role = getRole(user_from_request);

            String token = Base64.getEncoder().encodeToString(user_from_request.getLogin().getBytes(StandardCharsets.UTF_8));

            Cookie cookie = new Cookie("Authtoken", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setMaxAge(3600);

            cookie.setAttribute("SameSite", "None");

            response.addCookie(cookie);

            String jsonResponse = String.format("{\"message\":\"Poprawnie zalogowano\", \"role\":\"%s\"}", role);
            return ResponseEntity.ok(jsonResponse);
        } else {
            String errorResponse = "{\"message\":\"Nie istnieje taki login\"}";
            return ResponseEntity.status(404).body(errorResponse);
        }


    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@CookieValue(value = "Authtoken", required = false) String token) {

        if(token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body("{\"message\":\"Brakuje tokenu, sprobuj na innej przegladarce\"}");
        }

        String login;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(token);
            login = new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            System.out.println("(!)[REQUEST_LOG] at /api/auth/me POST Received sus token: " + token);
            return ResponseEntity.status(401).body("{\"message\":\"Uszkodzony token\"}");
        }

        Optional<User> user = userRepository.findByLogin(login);
        if (user.isEmpty()) {
            System.out.println("(!)[REQUEST_LOG] at /api/auth/me POST Received sus token: " + token);
            return ResponseEntity.status(401).body("{\"message\":\"Token nie nalezy do zadnego user\"}");
        }

        User user_from_request = user.get();
        String role = getRole(user_from_request);

        String jsonResponse = String.format("{\"message\":\"Token valid\", \"login\":\"%s\", \"role\":\"%s\"}", login, role);
        return ResponseEntity.ok().body(jsonResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Clear cookie by setting it with maxAge=0
        ResponseCookie cookie = ResponseCookie.from("Authtoken", "")
                .httpOnly(true)
                .secure(true) // use true if you're on HTTPS
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }

}
