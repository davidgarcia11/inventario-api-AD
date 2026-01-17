package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.entity.*;
import com.example.inventarioapiad.service.CompraService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CompraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompraService compraService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCrearCompra201() throws Exception {
        Compra compra = new Compra();
        compra.setProveedor(new Proveedor());
        compra.getProveedor().setId(1L);
        compra.setProducto(new Producto());
        compra.getProducto().setId(1L);
        compra.setAlmacen(new Almacen());
        compra.getAlmacen().setId(1L);
        compra.setCantidad(100);
        compra.setPrecioUnitario(1.0f);
        compra.setFechaCompra(LocalDateTime.now());

        Compra compraCreada = new Compra();
        compraCreada.setId(1L);
        compraCreada.setCantidad(100);

        when(compraService.crear(any(Compra.class))).thenReturn(compraCreada);

        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compra)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCrearCompra400() throws Exception {
        Compra compra = new Compra();

        doThrow(new IllegalArgumentException("El proveedor es obligatorio"))
                .when(compraService).crear(any(Compra.class));

        mockMvc.perform(post("/api/compras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compra)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    public void testBuscarPorId200() throws Exception {
        Compra compra = new Compra();
        compra.setId(1L);
        compra.setCantidad(100);

        when(compraService.buscarPorId(1L)).thenReturn(compra);

        mockMvc.perform(get("/api/compras/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testBuscarPorId404() throws Exception {
        doThrow(new RuntimeException("Compra no encontrada con ID: 999"))
                .when(compraService).buscarPorId(999L);

        mockMvc.perform(get("/api/compras/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    @Test
    public void testBuscarTodos200() throws Exception {
        mockMvc.perform(get("/api/compras"))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizar200() throws Exception {
        Compra compraActualizada = new Compra();
        compraActualizada.setEstado("RECIBIDA");

        Compra result = new Compra();
        result.setId(1L);
        result.setEstado("RECIBIDA");

        when(compraService.actualizar(eq(1L), any(Compra.class)))
                .thenReturn(result);

        mockMvc.perform(put("/api/compras/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraActualizada)))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizar404() throws Exception {
        Compra compraActualizada = new Compra();

        doThrow(new RuntimeException("Compra no encontrada con ID: 999"))
                .when(compraService).actualizar(eq(999L), any(Compra.class));

        mockMvc.perform(put("/api/compras/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(compraActualizada)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminar204() throws Exception {
        mockMvc.perform(delete("/api/compras/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testEliminar404() throws Exception {
        doThrow(new RuntimeException("Compra no encontrada con ID: 999"))
                .when(compraService).eliminar(999L);

        mockMvc.perform(delete("/api/compras/999"))
                .andExpect(status().isNotFound());
    }
}