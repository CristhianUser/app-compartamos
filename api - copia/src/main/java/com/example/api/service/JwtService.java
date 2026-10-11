package com.example.api.service;

import com.example.api.entity.Usuario;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface JwtService {
    String extractUsername(String token);
    String generateToken(UserDetails userDetails, Usuario usuario);
    String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails);
    boolean isRefreshToken(String token);
    boolean validateToken(String token, UserDetails userDetails);
}
