package com.example.api.service;

import com.example.api.agregates.requests.SigninRequest;
import com.example.api.agregates.requests.SignupRequest;
import com.example.api.agregates.responses.SigninResponse;
import com.example.api.entity.Usuario;

import java.util.List;

public interface AuthService {
    Usuario signupUser(SignupRequest signupRequest);
    Usuario signupAdmin(SignupRequest signupRequest);
    SigninResponse signIn(SigninRequest signinRequest);
    List<Usuario> listUsuarios();
}
