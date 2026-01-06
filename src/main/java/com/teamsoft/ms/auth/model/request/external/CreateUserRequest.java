package com.teamsoft.ms.auth.model.request.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateUserRequest {
    @JsonProperty("document_type_id")
    private String documentTypeId;

    @JsonProperty("identification_number")
    private String identificationNumber;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("first_last_name")
    private String firstLastName;

    @JsonProperty("second_last_name")
    private String secondLastName;

    @JsonProperty("address")
    private String address;

    @JsonProperty("phone")
    private String phone;
}
