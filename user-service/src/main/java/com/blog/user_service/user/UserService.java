package com.blog.user_service.user;

import com.blog.user_service.user.DTO.UserResponse;
import com.blog.user_service.util.ApiResponse;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public ApiResponse<?> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            if (!users.isEmpty()) {
                List<UserResponse> userResponses = users.stream()
                        .map(user -> modelMapper.map(user, UserResponse.class))
                        .collect(Collectors.toList());

                return new ApiResponse<>("Users fetched successfully.", userResponses, HttpStatus.OK.value());
            } else {
                return new ApiResponse<>("No users found.", null, HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            log.error("An error occurred while fetching users.", e);
            return new ApiResponse<>("An error occurred while fetching users.", null, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    public ApiResponse<?> getUserProfile(Principal principal) {
        try {
            // Get the email (or username) of the currently authenticated user
            String email = principal.getName();

            // Use the email to fetch the user's profile from the database
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UserResponse userResponse = modelMapper.map(user, UserResponse.class);

                return new ApiResponse<>("User profile fetched successfully.", userResponse, HttpStatus.OK.value());
            } else {
                return new ApiResponse<>("User not found.", null, HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            log.error("An error occurred while fetching user profile.", e);
            return new ApiResponse<>("An error occurred while fetching user profile.", null, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
