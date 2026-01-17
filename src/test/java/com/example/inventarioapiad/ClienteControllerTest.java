package com.example.inventarioapiad.controller;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCrearCliente201() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNombre("Constructora López");
        cliente.setEmail("ventas@constructoralopez.com");

        Cliente clienteCreado = new Cliente();
        clienteCreado.setId(1L);
        clienteCreado.setNombre("Constructora López");
        clienteCreado.setEmail("ventas@constructoralopez.com");

        when(clienteService.crear(any(Cliente.class))).thenReturn(clienteCreado);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCrearCliente400() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNombre("");

        doThrow(new IllegalArgumentException("El nombre del cliente es obligatorio"))
                .when(clienteService).crear(any(Cliente.class));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    public void testBuscarPorId200() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Constructora López");

        when(clienteService.buscarPorId(1L)).thenReturn(cliente);

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testBuscarPorId404() throws Exception {
        doThrow(new RuntimeException("Cliente no encontrado con ID: 999"))
                .when(clienteService).buscarPorId(999L);

        mockMvc.perform(get("/api/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    @Test
    public void testBuscarTodos200() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizar200() throws Exception {
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setNombre("Constructora García");

        Cliente result = new Cliente();
        result.setId(1L);
        result.setNombre("Constructora García");

        when(clienteService.actualizar(eq(1L), any(Cliente.class)))
                .thenReturn(result);

        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteActualizado)))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizar404() throws Exception {
        Cliente clienteActualizado = new Cliente();

        doThrow(new RuntimeException("Cliente no encontrado con ID: 999"))
                .when(clienteService).actualizar(eq(999L), any(Cliente.class));

        mockMvc.perform(put("/api/clientes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteActualizado)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminar204() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testEliminar404() throws Exception {
        doThrow(new RuntimeException("Cliente no encontrado con ID: 999"))
                .when(clienteService).eliminar(999L);

        mockMvc.perform(delete("/api/clientes/999"))
                .andExpect(status().isNotFound());
    }
}