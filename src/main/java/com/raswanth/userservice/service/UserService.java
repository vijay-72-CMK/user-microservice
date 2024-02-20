package com.raswanth.userservice.service;

import com.raswanth.userservice.dto.*;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;

public interface UserService {
    void registerUser(UserRegistrationDTO userDTO);

    List<ViewUsersResponseDTO> getAllUsers();

    ResponseEntity<String> sigin(SignInRequestDTO signInRequestDTO);

    void deleteUser(String username);

    void changePassword(ChangePasswordRequestDto changePasswordRequestDto, Principal singedInUser);

    void addAddress(AddressRequestDTO addressRequest, Principal signedInUser);
}
