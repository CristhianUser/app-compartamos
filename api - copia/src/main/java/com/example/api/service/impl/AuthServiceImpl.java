package com.example.api.service.impl;

import com.example.api.agregates.constants.MapString;
import com.example.api.agregates.requests.SigninRequest;
import com.example.api.agregates.requests.SignupRequest;
import com.example.api.agregates.responses.ReniecResponse;
import com.example.api.agregates.responses.SigninResponse;
import com.example.api.configuration.RestClientConfig;
import com.example.api.entity.Role;
import com.example.api.entity.Rol;
import com.example.api.entity.Usuario;
import com.example.api.repository.RolRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.AuthService;
import com.example.api.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RestClientConfig restClientConfig;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @Override
    public Usuario signupUser(SignupRequest signupRequest) {
        Usuario usuario = getEntity(signupRequest);
        Rol rol_user = getRoles(Role.USER);
        Set<Rol> roles = new HashSet<>();
        roles.add(rol_user);
        usuario.setRoles(roles);
        return userRepository.save(usuario);
    }

    @Override
    public Usuario signupAdmin(SignupRequest signupRequest) {
        Usuario usuario = getEntity(signupRequest);
        Rol rol_admin = getRoles(Role.ADMIN);
        Rol rol_user = getRoles(Role.USER);
        Set<Rol> roles = new HashSet<>();
        roles.add(rol_admin);
        roles.add(rol_user);
        usuario.setRoles(roles);
        return userRepository.save(usuario);
    }

    @Override
    public SigninResponse signIn(SigninRequest signinRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                signinRequest.getEmail(),signinRequest.getPassword()
        ));

        var Usuario = userRepository.findByEmail(signinRequest.getEmail());
        var token = jwtService.generateToken(Usuario, Usuario);
        var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), Usuario);
        return SigninResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Cacheable(value = "usuarios")
    public List<Usuario> listUsuarios() {
        System.out.println("CONSULTANDO EN LA BASE DE DATOS (POSTGRES SQL)");
        return userRepository.findAll();
    }

    public ReniecResponse getUserByDni(String numDoc){
        return restClientConfig.restClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/reniec/dni")
                        .queryParam("numero", numDoc)
                        .build())
                .retrieve()
                .body(ReniecResponse.class);
    };

    public Usuario getEntity(SignupRequest signupRequest){
        ReniecResponse reniecResponse = getUserByDni(signupRequest.getNumDoc());
        return Usuario.builder()
                .nombreCompleto(reniecResponse.getFull_name())
                .nombres(reniecResponse.getFirst_name())
                .apellidoPaterno(reniecResponse.getFirst_last_name())
                .apellidoMaterno(reniecResponse.getSecond_last_name())
                .documento(reniecResponse.getDocument_number())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .isAccountNonExpired(MapString.STATUS_ACTIVE)
                .isAccountNonLocked(MapString.STATUS_ACTIVE)
                .isCredentialsNonExpired(MapString.STATUS_ACTIVE)
                .isEnabled(MapString.STATUS_ACTIVE)
                .build();
    }

    public Rol getRoles(Role role){
        return rolRepository.findByNombreRol(role.name());
    }

}
