package com.raswanth.userservice.service.impl;

import com.raswanth.userservice.dto.RoleDTO;
import com.raswanth.userservice.entity.RoleEntity;
import com.raswanth.userservice.repositories.RoleRepository;
import com.raswanth.userservice.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<RoleDTO> getAllRoles() {
        List<RoleEntity> roles = roleRepository.findAll();
        return roles.stream()
                .map(role -> new RoleDTO(role.getId(), role.getName(), role.getDescription()))
                .collect(Collectors.toList());
    }
}
