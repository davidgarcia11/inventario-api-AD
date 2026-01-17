package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.entity.Proveedor;
import com.example.inventarioapiad.service.ProveedorService;
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
public class ProveedorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProveedorService proveedorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCrearProveedor201() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Aceros García");
        proveedor.setEmail("contacto@acerosgarcia.com");

        Proveedor proveedorCreado = new Proveedor();
        proveedorCreado.setId(1L);
        proveedorCreado.setNombre("Aceros García");
        proveedorCreado.setEmail("contacto@acerosgarcia.com");

        when(proveedorService.crear(any(Proveedor.class))).thenReturn(proveedorCreado);

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proveedor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCrearProveedor400() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("");

        doThrow(new IllegalArgumentException("El nombre del proveedor es obligatorio"))
                .when(proveedorService).crear(any(Proveedor.class));

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proveedor)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    public void testBuscarPorId200() throws Exception {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setNombre("Aceros García");

        when(proveedorService.buscarPorId(1L)).thenReturn(proveedor);

        mockMvc.perform(get("/api/proveedores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testBuscarPorId404() throws Exception {
        doThrow(new RuntimeException("Proveedor no encontrado con ID: 999"))
                .when(proveedorService).buscarPorId(999L);

        mockMvc.perform(get("/api/proveedores/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    @Test
    public void testBuscarTodos200() throws Exception {
        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizar200() throws Exception {
        Proveedor proveedorActualizado = new Proveedor();
        proveedorActualizado.setNombre("Aceros López");

        Proveedor result = new Proveedor();
        result.setId(1L);
        result.setNombre("Aceros López");

        when(proveedorService.actualizar(eq(1L), any(Proveedor.class)))
                .thenReturn(result);

        mockMvc.perform(put("/api/proveedores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proveedorActualizado)))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizar404() throws Exception {
        Proveedor proveedorActualizado = new Proveedor();

        doThrow(new RuntimeException("Proveedor no encontrado con ID: 999"))
                .when(proveedorService).actualizar(eq(999L), any(Proveedor.class));

        mockMvc.perform(put("/api/proveedores/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(proveedorActualizado)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminar204() throws Exception {
        mockMvc.perform(delete("/api/proveedores/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testEliminar404() throws Exception {
        doThrow(new RuntimeException("Proveedor no encontrado con ID: 999"))
                .when(proveedorService).eliminar(999L);

        mockMvc.perform(delete("/api/proveedores/999"))
                .andExpect(status().isNotFound());
    }
}