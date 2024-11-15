package com.blog.user_service.user;

import com.blog.user_service.auth.AuthenticationService;
import com.blog.user_service.util.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        ApiResponse<?> response = userService.getAllUsers();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Principal principal) {
        ApiResponse<?> response = userService.getUserProfile(principal);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
