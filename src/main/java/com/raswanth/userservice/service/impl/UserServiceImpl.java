package com.raswanth.userservice.service.impl;

import com.raswanth.userservice.dto.UserRegistrationDTO;
import com.raswanth.userservice.entity.RoleEntity;
import com.raswanth.userservice.entity.UserEntity;
import com.raswanth.userservice.exception.UserAlreadyExistsException;
import com.raswanth.userservice.repositories.RoleRepository;
import com.raswanth.userservice.repositories.UserRepository;
import com.raswanth.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public void registerUser(UserRegistrationDTO userDTO) {
        Optional<UserEntity> existingUser = userRepository.findByUsername(userDTO.getUsername());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("Username is already used, try another one");
        }

        existingUser = userRepository.findByEmail(userDTO.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("Email is already registered, try another one");
        }
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
    }
}
