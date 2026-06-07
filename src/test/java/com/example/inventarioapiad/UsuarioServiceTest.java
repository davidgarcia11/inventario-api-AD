package com.example.inventarioapiad;

import com.example.inventarioapiad.dto.AuthResponse;
import com.example.inventarioapiad.dto.LoginRequest;
import com.example.inventarioapiad.dto.RegisterRequest;
import com.example.inventarioapiad.entity.Usuario;
import com.example.inventarioapiad.repository.UsuarioRepository;
import com.example.inventarioapiad.security.JwtService;
import com.example.inventarioapiad.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Tests unitarios del UsuarioService: cubrimos el registro y el login,
// tanto el caso bueno como los errores típicos (usuario duplicado y
// contraseña incorrecta).
@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioService usuarioService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    public void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("david");
        registerRequest.setPassword("secreto123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("david");
        loginRequest.setPassword("secreto123");
    }

    // -------- REGISTRO --------

    @Test
    public void registrar_usuarioNuevo_devuelveToken() {
        when(usuarioRepository.existsByUsername("david")).thenReturn(false);
        when(passwordEncoder.encode("secreto123")).thenReturn("hash-bcrypt");
        when(jwtService.generarToken("david")).thenReturn("token-jwt");
        when(jwtService.getExpiracionEnSegundos()).thenReturn(3600L);

        AuthResponse respuesta = usuarioService.registrar(registerRequest);

        assertEquals("token-jwt", respuesta.getToken());
        assertEquals("david", respuesta.getUsername());
        assertEquals(3600L, respuesta.getExpiresIn());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    public void registrar_usuarioDuplicado_lanzaExcepcion() {
        when(usuarioRepository.existsByUsername("david")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.registrar(registerRequest));

        assertTrue(ex.getMessage().toLowerCase().contains("ya existe"));
        verify(usuarioRepository, never()).save(any());
    }

    // -------- LOGIN --------

    @Test
    public void autenticar_credencialesCorrectas_devuelveToken() {
        Usuario usuarioBd = new Usuario();
        usuarioBd.setUsername("david");
        usuarioBd.setPassword("hash-bcrypt");

        when(usuarioRepository.findByUsername("david")).thenReturn(Optional.of(usuarioBd));
        when(passwordEncoder.matches("secreto123", "hash-bcrypt")).thenReturn(true);
        when(jwtService.generarToken("david")).thenReturn("token-jwt");
        when(jwtService.getExpiracionEnSegundos()).thenReturn(3600L);

        AuthResponse respuesta = usuarioService.autenticar(loginRequest);

        assertEquals("token-jwt", respuesta.getToken());
        assertEquals("david", respuesta.getUsername());
    }

    @Test
    public void autenticar_usuarioInexistente_lanzaExcepcion() {
        when(usuarioRepository.findByUsername("david")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.autenticar(loginRequest));

        assertTrue(ex.getMessage().toLowerCase().contains("incorrectos"));
        // No debe llegar a generar token
        verify(jwtService, never()).generarToken(anyString());
    }

    @Test
    public void autenticar_passwordIncorrecta_lanzaExcepcion() {
        Usuario usuarioBd = new Usuario();
        usuarioBd.setUsername("david");
        usuarioBd.setPassword("hash-bcrypt");

        when(usuarioRepository.findByUsername("david")).thenReturn(Optional.of(usuarioBd));
        when(passwordEncoder.matches("secreto123", "hash-bcrypt")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.autenticar(loginRequest));

        assertTrue(ex.getMessage().toLowerCase().contains("incorrectos"));
        verify(jwtService, never()).generarToken(anyString());
    }
}
