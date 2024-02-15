package com.raswanth.userservice.service;

import com.raswanth.userservice.dto.UserRegistrationDTO;

public interface UserService {
    void registerUser(UserRegistrationDTO userDTO);
}
