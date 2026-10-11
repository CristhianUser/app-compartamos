package com.example.api.configuration;

import com.example.api.entity.Role;
import com.example.api.entity.Rol;
import com.example.api.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    public void run(String... args) throws Exception {
        if(rolRepository.count() == 0){
            Rol rol_user = new Rol();
            Rol rol_admin = new Rol();
            rol_user.setNombreRol(Role.USER.name());
            rol_admin.setNombreRol(Role.ADMIN.name());
            Set<Rol> roles = new HashSet<>();
            roles.add(rol_user);
            roles.add(rol_admin);
            rolRepository.saveAll(roles);
            System.out.println("Roles creados correctamente");
        }
    }
}
