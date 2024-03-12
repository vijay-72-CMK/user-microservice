package com.raswanth.userservice.service.impl;

import com.raswanth.userservice.dto.AddressRequestDTO;
import com.raswanth.userservice.dto.ChangePasswordRequestDto;
import com.raswanth.userservice.dto.RoleDTO;
import com.raswanth.userservice.dto.SignInRequestDTO;
import com.raswanth.userservice.dto.UserRegistrationDTO;
import com.raswanth.userservice.dto.ViewUsersResponseDTO;
import com.raswanth.userservice.entity.AddressEntity;
import com.raswanth.userservice.entity.RoleEntity;
import com.raswanth.userservice.entity.UserEntity;
import com.raswanth.userservice.exception.GeneralInternalException;
import com.raswanth.userservice.repositories.RoleRepository;
import com.raswanth.userservice.repositories.UserRepository;
import com.raswanth.userservice.service.JWTService;
import com.raswanth.userservice.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.Role;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    @Value("${jwt.expiration-time}")
    private long jwtExpirationTimeSeconds;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JWTService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public void registerUser(UserRegistrationDTO userDTO) {
        try {

            String encodedPassword = passwordEncoder.encode(userDTO.getPassword());

            UserEntity user = new UserEntity();
            user.setUsername(userDTO.getUsername());
            user.setPassword(encodedPassword);
            user.setEmail(userDTO.getEmail());
            user.setMobileNumber(userDTO.getMobileNumber());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEnabled(true);

            RoleEntity defaultRole = roleRepository.findByName("Customer");
            user.getRoles().add(defaultRole);

            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            log.error(ex.getMessage());
            throw new GeneralInternalException("User already exists, use a unique email and username", HttpStatus.BAD_REQUEST);
        } catch (DataAccessException ex) {
            throw new GeneralInternalException("Database error while registering user with username " + userDTO.getUsername());
        }
    }

    public ResponseEntity<String> sigin(SignInRequestDTO signInRequestDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequestDTO.getUsername(), signInRequestDTO.getPassword()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);
            HttpHeaders headers = new HttpHeaders();
            ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                    .maxAge(jwtExpirationTimeSeconds)
                    .httpOnly(true)
                    .sameSite("lax")
                    .path("/api")
                    .domain("localhost")
                    .secure(true)
                    .build();
            headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
            return ResponseEntity.ok().headers(headers).body("Logged in successfully!");
        } catch (BadCredentialsException ex) {
            throw new GeneralInternalException("Bad credentials(sign in), please enter correct username and password", HttpStatus.BAD_REQUEST);
        }
    }

    public void deleteUser(Long userId) {
        if (userRepository.deleteById(userId) == 0)
            throw new GeneralInternalException("Could not delete user as userId " + userId  + " does not exist", HttpStatus.NOT_FOUND);
    }

    @Override
    public void changePassword(@Valid ChangePasswordRequestDto changePasswordRequestDto, Principal signedInUser) {
        try {
            Long userId = Long.valueOf(signedInUser.getName());
            UserEntity user = userRepository.findById(userId).orElseThrow();

            // password checks
            if (!passwordEncoder.matches(changePasswordRequestDto.getCurrentPassword(), user.getPassword())) {
                throw new GeneralInternalException("Current password is not correct, try again", HttpStatus.BAD_REQUEST);
            }
            if (passwordEncoder.matches(changePasswordRequestDto.getNewPassword(), user.getPassword())) {
                throw new GeneralInternalException("New password cannot be same as old password", HttpStatus.BAD_REQUEST);
            }
            if (!changePasswordRequestDto.getNewPassword().equals(changePasswordRequestDto.getConfirmPassword())) {
                throw new GeneralInternalException("Passwords do not match, try again", HttpStatus.BAD_REQUEST);
            }

            user.setPassword(passwordEncoder.encode(changePasswordRequestDto.getConfirmPassword()));
            userRepository.save(user);
        } catch (DataAccessException ex) {
            throw new GeneralInternalException("Database error while updating password");
        }
    }

    @Override
    @Transactional
    public void addAddress(AddressRequestDTO addressRequest, Principal signedInUser) {
        try {
            Long userId = Long.valueOf(signedInUser.getName());
            UserEntity currUser = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralInternalException("User id not found while adding address", HttpStatus.NOT_FOUND));

            AddressEntity addressEntity = AddressEntity.builder()
                    .street(addressRequest.getStreet())
                    .city(addressRequest.getCity())
                    .state(addressRequest.getState())
                    .zipCode(addressRequest.getZipCode())
                    .build();
            currUser.getAddressEntities().add(addressEntity);
            userRepository.save(currUser);
        } catch (DataAccessException ex) {
            throw new GeneralInternalException("Database error while trying to add address");
        }
    }

    @Override
    public ViewUsersResponseDTO getUserById(Long id) {
        try {
            UserEntity user = userRepository.findById(id)
                    .orElseThrow(() -> new GeneralInternalException("Cannot get user as Id does not exist", HttpStatus.NOT_FOUND));

            return ViewUsersResponseDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .lastName(user.getLastName())
                    .firstName(user.getFirstName())
                    .addressEntities(user.getAddressEntities())
                    .mobileNumber(user.getMobileNumber())
                    .username(user.getUsername())
                    .roles(user.getRoles())
                    .build();
        } catch (DataAccessException ex) {
            throw new GeneralInternalException("Database error while trying to add view user with id: " + id);
        }
    }

    @Override
    @Transactional
    public void changeUserRoles(Integer userId, List<RoleDTO> roleDTOs) {
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralInternalException("User not found with id: " + userId, HttpStatus.NOT_FOUND));

            List<RoleEntity> roles = roleDTOs.stream()
                    .map(RoleDTO::getId)
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new GeneralInternalException("Invalid role ID: " + roleId)))
                    .toList();

            user.setRoles(new HashSet<>(roles));
            userRepository.save(user);
        } catch (DataAccessException e) {
            throw new GeneralInternalException("Database error while trying to modify roles for user with id: " + userId);
        }
    }

    @Override
    public List<ViewUsersResponseDTO> getAllUsers() {
        try {
            List<UserEntity> users = userRepository.findAll();
            List<ViewUsersResponseDTO> usersResponseDTOList = new ArrayList<>();
            for (UserEntity user : users) {
                usersResponseDTOList.add(
                        ViewUsersResponseDTO.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .lastName(user.getLastName())
                                .firstName(user.getFirstName())
                                .addressEntities(user.getAddressEntities())
                                .mobileNumber(user.getMobileNumber())
                                .username(user.getUsername())
                                .roles(user.getRoles())
                                .build());

            }
            return usersResponseDTOList;
        } catch (DataAccessException ex) {
            throw new GeneralInternalException("Database error while trying to add view all users");
        }
    }
}
