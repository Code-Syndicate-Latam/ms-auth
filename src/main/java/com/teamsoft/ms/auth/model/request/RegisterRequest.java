package com.teamsoft.ms.auth.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class RegisterRequest {
    @JsonProperty("document_type_id")
    public String documentTypeId;
    @JsonProperty("identification_number")
    public String identificationNumber;
    @JsonProperty("first_name")
    public String firstName;
    @JsonProperty("date_of_birth")
    public String dateOfBirth;
    @JsonProperty("middle_name")
    public String middleName;
    @JsonProperty("first_last_name")
    public String firstLastName;
    @JsonProperty("second_last_name")
    public String secondLastName;
    @JsonProperty("email")
    public  String email;
    @JsonProperty("password")
    public String password;
    @JsonProperty("phone")
    public String phone;
    @JsonProperty("rol")
    public String rol;
    @JsonProperty("address")
    public String address;

}
