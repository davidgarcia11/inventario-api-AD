package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.entity.*;
import com.example.inventarioapiad.service.VentaService;
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
public class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VentaService ventaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCrearVenta201() throws Exception {
        Venta venta = new Venta();
        venta.setCliente(new Cliente());
        venta.getCliente().setId(1L);
        venta.setProducto(new Producto());
        venta.getProducto().setId(1L);
        venta.setAlmacen(new Almacen());
        venta.getAlmacen().setId(1L);
        venta.setCantidad(50);
        venta.setPrecioUnitario(1.50f);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setNumeroPedido("PED-2025-0001");

        Venta ventaCreada = new Venta();
        ventaCreada.setId(1L);
        ventaCreada.setCantidad(50);

        when(ventaService.crear(any(Venta.class))).thenReturn(ventaCreada);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(venta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCrearVenta400() throws Exception {
        Venta venta = new Venta();

        doThrow(new IllegalArgumentException("El cliente es obligatorio"))
                .when(ventaService).crear(any(Venta.class));

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(venta)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    public void testBuscarPorId200() throws Exception {
        Venta venta = new Venta();
        venta.setId(1L);
        venta.setCantidad(50);

        when(ventaService.buscarPorId(1L)).thenReturn(venta);

        mockMvc.perform(get("/api/ventas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testBuscarPorId404() throws Exception {
        doThrow(new RuntimeException("Venta no encontrada con ID: 999"))
                .when(ventaService).buscarPorId(999L);

        mockMvc.perform(get("/api/ventas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    @Test
    public void testBuscarTodos200() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizar200() throws Exception {
        Venta ventaActualizada = new Venta();
        ventaActualizada.setEstado("ENTREGADA");

        Venta result = new Venta();
        result.setId(1L);
        result.setEstado("ENTREGADA");

        when(ventaService.actualizar(eq(1L), any(Venta.class)))
                .thenReturn(result);

        mockMvc.perform(put("/api/ventas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaActualizada)))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizar404() throws Exception {
        Venta ventaActualizada = new Venta();

        doThrow(new RuntimeException("Venta no encontrada con ID: 999"))
                .when(ventaService).actualizar(eq(999L), any(Venta.class));

        mockMvc.perform(put("/api/ventas/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaActualizada)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminar204() throws Exception {
        mockMvc.perform(delete("/api/ventas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testEliminar404() throws Exception {
        doThrow(new RuntimeException("Venta no encontrada con ID: 999"))
                .when(ventaService).eliminar(999L);

        mockMvc.perform(delete("/api/ventas/999"))
                .andExpect(status().isNotFound());
    }
}