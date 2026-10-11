package com.example.api.controller;

import com.example.api.entity.Usuario;
import com.example.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthService authService;

    @GetMapping("/users")
    private List<Usuario> listUsuarios(){
        return authService.listUsuarios();
    }

}
