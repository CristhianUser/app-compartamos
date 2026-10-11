package com.example.api.repository;

import com.example.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Usuario,Long> {
    Usuario findByEmail(String email);
}
