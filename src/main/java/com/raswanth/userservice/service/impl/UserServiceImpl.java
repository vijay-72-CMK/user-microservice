package com.raswanth.userservice.service.impl;

import com.raswanth.userservice.dto.JwtAuthenticationResponse;
import com.raswanth.userservice.dto.SignInRequestDTO;
import com.raswanth.userservice.dto.UserRegistrationDTO;
import com.raswanth.userservice.entity.RoleEntity;
import com.raswanth.userservice.entity.UserEntity;
import com.raswanth.userservice.exception.UserAlreadyExistsException;
import com.raswanth.userservice.repositories.RoleRepository;
import com.raswanth.userservice.repositories.UserRepository;
import com.raswanth.userservice.service.JWTService;
import com.raswanth.userservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
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
//        try {
//            userRepository.findByUsername(userDTO.getUsername())
//                    .ifPresent((user) -> {
//                        throw new UserAlreadyExistsException("Username already exists");
//                    });

            userRepository.findByEmail(userDTO.getEmail()).
                    ifPresent((user) ->  {
                       throw new UserAlreadyExistsException("Email already exists");
                    });

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
//        } catch (DataAccessException ex) {
//            log.error("Error occurred while registering user", ex);
//            throw new RuntimeException("Something went wrong, please try again latter");
//        }
    }

    public ResponseEntity<JwtAuthenticationResponse> sigin(SignInRequestDTO signInRequestDTO) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signInRequestDTO.getUsername(), signInRequestDTO.getPassword()));
        UserEntity user = userRepository.findByUsername(signInRequestDTO.getUsername()).orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        String token = jwtService.generateToken(user);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie","accessToken="+token+";Max-Age=3600;Secure; HttpOnly");

        return ResponseEntity.ok().headers(headers).body(new JwtAuthenticationResponse("Logged in succesfully!"));
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}
