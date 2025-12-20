package com.teamsoft.ms.auth.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterRequest {
    @JsonProperty("document_type")
    public String documentType;
    @JsonProperty("document_number")
    public String documentNumber;
    @JsonProperty("name")
    public String name;
    @JsonProperty("last_name")
    public String lastName;
    @JsonProperty("email")
    public  String email;
    @JsonProperty("password")
    public String password;
    @JsonProperty("phone")
    public String phone;
    @JsonProperty("rol")
    public String rol;

}
