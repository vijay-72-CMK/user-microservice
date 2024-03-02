package com.raswanth.userservice.controller;

import com.raswanth.userservice.dto.AddressRequestDTO;
import com.raswanth.userservice.dto.ChangePasswordRequestDto;
import com.raswanth.userservice.dto.SignInRequestDTO;
import com.raswanth.userservice.dto.UserRegistrationDTO;
import com.raswanth.userservice.dto.ViewUsersResponseDTO;
import com.raswanth.userservice.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegistrationDTO userDTO) {
        userService.registerUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody SignInRequestDTO signInRequestDTO) {
        return userService.sigin(signInRequestDTO);
    }

    @GetMapping("/all")
    public List<ViewUsersResponseDTO> viewUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable @NotNull(message = "Id cannot be blank") @Positive(message = "Id must be positive") Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully");
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequestDto changePasswordRequestDto,
                                            Principal signedInUser) {
        userService.changePassword(changePasswordRequestDto, signedInUser);
        return ResponseEntity.status(HttpStatus.OK).body("Changed !");
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> addAddress(@RequestBody @Valid AddressRequestDTO addressRequest,
                                        Principal signedInUser) {
        userService.addAddress(addressRequest, signedInUser);
        return ResponseEntity.status(HttpStatus.OK).body("Added !");
    }
    @GetMapping("/{userId}")
    public ViewUsersResponseDTO getById(@PathVariable("userId") @Positive Long userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/myInfo")
    public ViewUsersResponseDTO myInfo(Principal principal) {
        return userService.getUserById(Long.valueOf(principal.getName()));
    }

}
