package com.example.inventarioapiad;

import com.example.inventarioapiad.dto.AuthResponse;
import com.example.inventarioapiad.dto.LoginRequest;
import com.example.inventarioapiad.dto.RegisterRequest;
import com.example.inventarioapiad.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Tests del AuthController. Probamos los códigos HTTP que esperan los
// clientes: 200 cuando todo va bien, 400 para datos inválidos y 401
// para credenciales incorrectas.
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    // -------- REGISTER --------

    @Test
    public void registrarUsuario_devuelve200() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("david");
        req.setPassword("secreto123");

        when(usuarioService.registrar(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("token-jwt", "david", 3600L));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.username").value("david"));
    }

    @Test
    public void registrarUsuario_sinPassword_devuelve400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("david");
        // password vacía -> @NotBlank salta

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void registrarUsuario_duplicado_devuelve400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("david");
        req.setPassword("secreto123");

        when(usuarioService.registrar(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Ya existe un usuario con ese nombre"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    // -------- LOGIN --------

    @Test
    public void login_correcto_devuelve200() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("david");
        req.setPassword("secreto123");

        when(usuarioService.autenticar(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("token-jwt", "david", 3600L));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"));
    }

    @Test
    public void login_credencialesIncorrectas_devuelve401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("david");
        req.setPassword("mala");

        when(usuarioService.autenticar(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Usuario o contraseña incorrectos"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value(401));
    }

    @Test
    public void login_sinUsername_devuelve400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setPassword("secreto123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
