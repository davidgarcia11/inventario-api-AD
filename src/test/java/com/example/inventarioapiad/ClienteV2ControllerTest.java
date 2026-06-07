package com.example.inventarioapiad;

import com.example.inventarioapiad.dto.ClienteCreateRequest;
import com.example.inventarioapiad.entity.Cliente;
import com.example.inventarioapiad.service.ClienteService;
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

// Tests del endpoint V2 POST de clientes.
// Verificamos 201 con cabecera Location, 400 cuando faltan campos y
// 400 cuando el email tiene formato inválido.
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ClienteV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    public void crearCliente_valido_devuelve201YLocation() throws Exception {
        ClienteCreateRequest req = new ClienteCreateRequest();
        req.setNombre("Empresa SA");
        req.setEmail("contacto@empresa.com");
        req.setCiudad("Zaragoza");

        Cliente guardado = new Cliente();
        guardado.setId(42L);
        guardado.setNombre("Empresa SA");
        guardado.setEmail("contacto@empresa.com");

        when(clienteService.crear(any(Cliente.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/v2/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/v2/clientes/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.email").value("contacto@empresa.com"));
    }

    @Test
    public void crearCliente_sinEmail_devuelve400() throws Exception {
        ClienteCreateRequest req = new ClienteCreateRequest();
        req.setNombre("Empresa SA");
        // sin email

        mockMvc.perform(post("/api/v2/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void crearCliente_emailInvalido_devuelve400() throws Exception {
        ClienteCreateRequest req = new ClienteCreateRequest();
        req.setNombre("Empresa SA");
        req.setEmail("esto-no-es-un-email");

        mockMvc.perform(post("/api/v2/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
