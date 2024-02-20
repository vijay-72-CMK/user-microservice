package com.raswanth.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ViewUsersResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String mobileNumber;
    private String firstName;
    private String lastName;

}
