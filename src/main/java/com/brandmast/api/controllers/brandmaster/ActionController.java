package com.brandmast.api.controllers.brandmaster;

import com.brandmast.api.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bm")
public class ActionController {
    @Autowired
    private UserRepository userRepository;



    @GetMapping("/actions")
    public ResponseEntity<?> getActions(@CookieValue(value = "Authtoken", required = true) String authToken) {

    }
}
