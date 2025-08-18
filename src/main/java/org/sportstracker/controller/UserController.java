package org.sportstracker.controller;

import org.sportstracker.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public String getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getDetails();
        Long userId = user.getId();
        return "Authenticated user: " + user.getUsername() + ", id=" + userId;
    }

}
