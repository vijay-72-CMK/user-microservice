package com.raswanth.userservice.service;

import com.raswanth.userservice.dto.ChangePasswordRequestDto;
import com.raswanth.userservice.dto.JwtAuthenticationResponse;
import com.raswanth.userservice.dto.SignInRequestDTO;
import com.raswanth.userservice.dto.UserRegistrationDTO;
import com.raswanth.userservice.entity.UserEntity;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;

public interface UserService {
    void registerUser(UserRegistrationDTO userDTO);

    List<UserEntity> getAllUsers();

    ResponseEntity<JwtAuthenticationResponse> sigin(SignInRequestDTO signInRequestDTO);

    void deleteUser(String username);

    void changePassword(ChangePasswordRequestDto changePasswordRequestDto, Principal singedInUser);
}
