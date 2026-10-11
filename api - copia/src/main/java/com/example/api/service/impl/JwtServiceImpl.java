package com.example.api.service.impl;

import com.example.api.agregates.constants.MapString;
import com.example.api.entity.Usuario;
import com.example.api.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtServiceImpl implements JwtService {

    @Value("${key.security}")
    private String keySignature;

    @Override
    public String extractUsername(String token) {
        return extractClaim(token,Claims::getSubject);
    }

    @Override
    public String generateToken(UserDetails userDetails, Usuario usuario) {
        var JwtToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(addClaims(userDetails))
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 900000))
                .claim("type", MapString.ACCESS)
                .claim("name", usuario.getNombreCompleto())
                .claim("doc", usuario.getDocumento())
                .signWith(getSignKey(), SignatureAlgorithm.HS512)
                .compact();
        return JwtToken;
    }

    @Override
    public String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        var JwtRefreshToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("type", MapString.REFRESH)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 120000))
                .signWith(getSignKey(), SignatureAlgorithm.HS512)
                .compact();
        return JwtRefreshToken;
    }

    @Override
    public boolean isRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        String typeToken = claims.get("type", String.class);
        return MapString.REFRESH.equalsIgnoreCase(typeToken);
    }

    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    //DECODIFICAR LA KEY CREADA Y CODIFICADA EN BASE64
    private Key getSignKey(){
        byte[] key = Decoders.BASE64.decode(keySignature);
        return Keys.hmacShaKeyFor(key);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
    }

    private <T> T extractClaim(String token, Function<Claims,T> claimsTFunction){
        return claimsTFunction.apply(extractAllClaims(token));
    }

    private boolean isTokenExpired(String token){
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Map<String, Object> addClaims(UserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        claims.put(MapString.STATUS_AccountNonExpired, userDetails.isAccountNonExpired());
        claims.put(MapString.STATUS_AccountNonLocked, userDetails.isAccountNonLocked());
        claims.put(MapString.STATUS_CredentialsNonExpired, userDetails.isCredentialsNonExpired());
        claims.put(MapString.STATUS_Enabled, userDetails.isEnabled());
        claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        return claims;
    }

}
