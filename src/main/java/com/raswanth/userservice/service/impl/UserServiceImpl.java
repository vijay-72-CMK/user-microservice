package com.raswanth.userservice.service.impl;

import com.raswanth.userservice.dto.AddressRequestDTO;
import com.raswanth.userservice.dto.ChangePasswordRequestDto;
import com.raswanth.userservice.dto.JwtAuthenticationResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;


@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {
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
            throw new GeneralInternalException("User already exists, use a unique email and username", HttpStatus.BAD_REQUEST);
        }
        catch (DataAccessException ex) {
            throw new GeneralInternalException("Database error while registering user with username " + userDTO.getUsername());
        }
    }

    public ResponseEntity<JwtAuthenticationResponse> sigin(SignInRequestDTO signInRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequestDTO.getUsername(), signInRequestDTO.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        HttpHeaders headers = new HttpHeaders();
        long expiry = 259200;
        headers.add("Set-Cookie","accessToken="+token+";Max-Age="+expiry+";Secure; HttpOnly");

        return ResponseEntity.ok().headers(headers).body(new JwtAuthenticationResponse("Logged in succesfully!"));
    }

    public void deleteUser(String username) {
        if (userRepository.deleteByUsername(username) == 0) throw new GeneralInternalException("Could not delete user as username does not exist", HttpStatus.NOT_FOUND);
    }

    @Override
    public void changePassword(ChangePasswordRequestDto changePasswordRequestDto, Principal signedInUser) {
        try {
            UserEntity user = (UserEntity) ((UsernamePasswordAuthenticationToken) signedInUser).getPrincipal();

            // password checks
            if (!passwordEncoder.matches(changePasswordRequestDto.getCurrentPassword(), user.getPassword())) {
                throw new GeneralInternalException("Current password is not correct, try again", HttpStatus.BAD_REQUEST);
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
        UserEntity userDetails = (UserEntity) ((UsernamePasswordAuthenticationToken) signedInUser).getPrincipal();

        UserEntity currUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new GeneralInternalException("User id not found while adding address", HttpStatus.NOT_FOUND));

        AddressEntity addressEntity = AddressEntity.builder()
                .street(addressRequest.getStreet())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .zipCode(addressRequest.getZipCode())
                .build();
        currUser.getAddressEntities().add(addressEntity);
        userRepository.save(currUser);
    }

    @Override
    public List<ViewUsersResponseDTO> getAllUsers() {
        return userRepository.findAllUsers();
    }
}
