package com.example.api.agregates.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SigninResponse {
    private String token;
    private String refreshToken;
}
