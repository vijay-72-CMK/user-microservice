package com.raswanth.userservice.dto;

import com.raswanth.userservice.entity.AddressEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
@Builder
public class ViewUsersResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String mobileNumber;
    private String firstName;
    private String lastName;

    private Set<AddressEntity> addressEntities;

}
