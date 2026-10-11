package com.example.api.controller;

import com.example.api.agregates.requests.SigninRequest;
import com.example.api.agregates.requests.SignupRequest;
import com.example.api.agregates.responses.SigninResponse;
import com.example.api.entity.Usuario;
import com.example.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signUser")
    private ResponseEntity<Usuario> createUser(@RequestBody SignupRequest signupRequest){
        return ResponseEntity.ok(authService.signupUser(signupRequest));
    }

    @PostMapping("/signAdmin")
    private ResponseEntity<Usuario> createAdmin(@RequestBody SignupRequest signupRequest){
        return ResponseEntity.ok(authService.signupAdmin(signupRequest));
    }

    @PostMapping("/signIn")
    private ResponseEntity<SigninResponse> login(@RequestBody SigninRequest signinRequest){
        return ResponseEntity.ok(authService.signIn(signinRequest));
    }

}
