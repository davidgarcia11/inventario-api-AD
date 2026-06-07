package com.example.inventarioapiad.service;

import com.example.inventarioapiad.dto.AuthResponse;
import com.example.inventarioapiad.dto.LoginRequest;
import com.example.inventarioapiad.dto.RegisterRequest;
import com.example.inventarioapiad.entity.Usuario;
import com.example.inventarioapiad.repository.UsuarioRepository;
import com.example.inventarioapiad.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Lógica de negocio relacionada con los usuarios: registrar uno nuevo y
// autenticar a uno existente para devolverle un JWT.
@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Crea un usuario nuevo en la BD. Devuelve directamente el token para
    // que el cliente pueda usar la API después del registro sin tener que
    // hacer un login extra.
    public AuthResponse registrar(RegisterRequest request) {
        log.info("Intentando registrar usuario: {}", request.getUsername());

        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario con ese nombre");
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(request.getUsername());
        // Importante: nunca guardar la contraseña en claro
        nuevo.setPassword(passwordEncoder.encode(request.getPassword()));

        usuarioRepository.save(nuevo);
        log.info("Usuario registrado correctamente: {}", nuevo.getUsername());

        String token = jwtService.generarToken(nuevo.getUsername());
        return new AuthResponse(token, nuevo.getUsername(),
                jwtService.getExpiracionEnSegundos());
    }

    // Comprueba las credenciales y, si son válidas, devuelve un token JWT.
    public AuthResponse autenticar(LoginRequest request) {
        log.info("Intentando login de: {}", request.getUsername());

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            // Mismo mensaje genérico que cuando no existe el usuario,
            // para no dar pistas a un atacante de qué falla.
            throw new IllegalArgumentException(
                    "Usuario o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario.getUsername());
        log.info("Login correcto: {}", usuario.getUsername());
        return new AuthResponse(token, usuario.getUsername(),
                jwtService.getExpiracionEnSegundos());
    }
}
