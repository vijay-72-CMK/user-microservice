package com.raswanth.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class RoleDTO {
    @NotNull
    private Integer id;
    @NotBlank
    private String name;
    private String description;
}
