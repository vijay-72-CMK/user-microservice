package com.raswanth.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequestDto {
    @NotBlank(message = "Password cannot be empty")
    private String currentPassword;

    @NotBlank(message = "New password cannot be empty")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must have one upper, one lower and one special char and have a length of at least 8")
    private String newPassword;

    @NotBlank(message = "New password cannot be empty")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "Password must have one upper, one lower and one special char and have a length of at least 8")
    private String confirmPassword;
}
