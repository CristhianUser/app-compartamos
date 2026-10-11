package com.example.api.agregates.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {
    private String numDoc;
    private String email;
    private String password;
}
