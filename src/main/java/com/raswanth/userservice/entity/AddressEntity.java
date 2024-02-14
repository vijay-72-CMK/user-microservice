package com.raswanth.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "addresses")
@Data
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Street is required")
    @Column(length = 100)
    private String street;

    @NotBlank(message = "City is required")
    @Column(length = 50)
    private String city;

    @NotBlank(message = "State is required")
    @Column(length = 50)
    private String state;

    @NotBlank(message = "Zip code is required")
    @Column(length = 40)
    private String zipCode;
}