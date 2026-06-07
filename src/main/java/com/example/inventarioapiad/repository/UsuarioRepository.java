package com.example.inventarioapiad.repository;

import com.example.inventarioapiad.entity.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

// Repositorio de usuarios. Solo necesitamos buscar por username (para
// el login) y comprobar si existe (para evitar duplicados en register).
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}
