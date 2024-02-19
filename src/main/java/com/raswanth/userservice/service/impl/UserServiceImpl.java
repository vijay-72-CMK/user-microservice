package com.raswanth.userservice.service.impl;

import com.raswanth.userservice.dto.JwtAuthenticationResponse;
import com.raswanth.userservice.dto.SignInRequestDTO;
import com.raswanth.userservice.dto.UserRegistrationDTO;
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
        headers.add("Set-Cookie","accessToken="+token+";Max-Age=3600;Secure; HttpOnly");

        return ResponseEntity.ok().headers(headers).body(new JwtAuthenticationResponse("Logged in succesfully!"));
    }

    public void deleteUser(String username) {
        userRepository.findByUsername(username).orElseThrow(() -> new GeneralInternalException("Cannot delete as username does not exist", HttpStatus.NOT_FOUND));
        userRepository.deleteByUsername(username);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}
