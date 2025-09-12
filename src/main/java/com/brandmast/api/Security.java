package com.brandmast.api;

import com.brandmast.api.entity.Brandmaster;
import com.brandmast.api.entity.User;
import com.brandmast.api.repository.BrandmasterRepository;
import com.brandmast.api.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class Security {
    public boolean success;
    public String message;
    public Object data;

    public Security(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;

    }

    public static Security check_security_BM(String token, UserRepository userRepository, BrandmasterRepository brandmasterRepository) {
        if (token == null) {
            return new Security(false, "Missing token", null);
        }

        String login;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(token);
            login = new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return new Security(false, "Invalid token", null);
        }

        Optional<User> userOpt = userRepository.findByLogin(login);
        if (userOpt.isEmpty()) {
            return new Security(false, "User not found", null);
        }

        Optional<Brandmaster> bmOpt = brandmasterRepository.findByUser_IdUser(userOpt.get().getIdUser());
        if (bmOpt.isEmpty()) {
            return new Security(false, "You are not a brandmaster", null);
        }

        return new Security(true, "Success", bmOpt.get());
    }
}
