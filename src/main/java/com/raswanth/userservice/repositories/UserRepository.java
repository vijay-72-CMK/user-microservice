package com.raswanth.userservice.repositories;

import com.raswanth.userservice.dto.ViewUsersResponseDTO;
import com.raswanth.userservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);

    Long deleteByUsername(String username);

    @Query("SELECT new com.raswanth.userservice.dto.ViewUsersResponseDTO(u.id, u.username, u.email, u.mobileNumber, u.firstName, u.lastName) FROM UserEntity u")
    List<ViewUsersResponseDTO> findAllUsers();
}
