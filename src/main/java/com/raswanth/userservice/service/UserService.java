package com.raswanth.userservice.service;

import com.raswanth.userservice.dto.AddressRequestDTO;
import com.raswanth.userservice.dto.ChangePasswordRequestDto;
import com.raswanth.userservice.dto.RoleDTO;
import com.raswanth.userservice.dto.SignInRequestDTO;
import com.raswanth.userservice.dto.UserRegistrationDTO;
import com.raswanth.userservice.dto.ViewUsersResponseDTO;
import org.springframework.http.ResponseEntity;

import javax.management.relation.Role;
import java.security.Principal;
import java.util.List;

public interface UserService {
    void registerUser(UserRegistrationDTO userDTO);

    List<ViewUsersResponseDTO> getAllUsers();

    ResponseEntity<String> sigin(SignInRequestDTO signInRequestDTO);

    void deleteUser(Long id);

    void changePassword(ChangePasswordRequestDto changePasswordRequestDto, Principal singedInUser);

    void addAddress(AddressRequestDTO addressRequest, Principal signedInUser);

    ViewUsersResponseDTO getUserById(Long id);

    void changeUserRoles(Integer userId, List<RoleDTO> roleDtos);
}
