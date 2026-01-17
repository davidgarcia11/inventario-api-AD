package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.entity.Producto;
import com.example.inventarioapiad.service.ProductoService;
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
public class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @Autowired
    private ObjectMapper objectMapper;

    // TEST 1: POST /api/productos - 201 CREATED
    @Test
    public void testCrearProducto201() throws Exception {
        Producto producto = new Producto();
        producto.setNombre("Tornillo M10");
        producto.setSku("TORNILLO-M10");
        producto.setPrecioVenta(1.25f);
        producto.setStockTotal(100);

        Producto productoCreado = new Producto();
        productoCreado.setId(1L);
        productoCreado.setNombre("Tornillo M10");
        productoCreado.setSku("TORNILLO-M10");
        productoCreado.setPrecioVenta(1.25f);
        productoCreado.setStockTotal(100);

        when(productoService.crear(any(Producto.class))).thenReturn(productoCreado);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Tornillo M10"));
    }

    // TEST 2: POST /api/productos - 400 BAD REQUEST
    @Test
    public void testCrearProducto400() throws Exception {
        Producto producto = new Producto();
        producto.setNombre("");  // Nombre vacío

        doThrow(new IllegalArgumentException("El nombre del producto es obligatorio"))
                .when(productoService).crear(any(Producto.class));

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    // TEST 3: GET /api/productos/{id} - 200 OK
    @Test
    public void testBuscarPorId200() throws Exception {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Tornillo M10");
        producto.setSku("TORNILLO-M10");
        producto.setPrecioVenta(1.25f);

        when(productoService.buscarPorId(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Tornillo M10"));
    }

    // TEST 4: GET /api/productos/{id} - 404 NOT FOUND
    @Test
    public void testBuscarPorId404() throws Exception {
        doThrow(new RuntimeException("Producto no encontrado con ID: 999"))
                .when(productoService).buscarPorId(999L);

        mockMvc.perform(get("/api/productos/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    // TEST 5: GET /api/productos - 200 OK
    @Test
    public void testBuscarTodos200() throws Exception {
        mockMvc.perform(get("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // TEST 6: PUT /api/productos/{id} - 200 OK
    @Test
    public void testActualizar200() throws Exception {
        Producto productoActualizado = new Producto();
        productoActualizado.setNombre("Tornillo M12");
        productoActualizado.setPrecioVenta(1.50f);

        Producto productoActualizadoResult = new Producto();
        productoActualizadoResult.setId(1L);
        productoActualizadoResult.setNombre("Tornillo M12");
        productoActualizadoResult.setPrecioVenta(1.50f);

        when(productoService.actualizar(eq(1L), any(Producto.class)))
                .thenReturn(productoActualizadoResult);

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tornillo M12"));
    }

    // TEST 7: PUT /api/productos/{id} - 404 NOT FOUND
    @Test
    public void testActualizar404() throws Exception {
        Producto productoActualizado = new Producto();
        productoActualizado.setNombre("Tornillo M12");

        doThrow(new RuntimeException("Producto no encontrado con ID: 999"))
                .when(productoService).actualizar(eq(999L), any(Producto.class));

        mockMvc.perform(put("/api/productos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoActualizado)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    // TEST 8: DELETE /api/productos/{id} - 204 NO CONTENT
    @Test
    public void testEliminar204() throws Exception {
        mockMvc.perform(delete("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    // TEST 9: DELETE /api/productos/{id} - 404 NOT FOUND
    @Test
    public void testEliminar404() throws Exception {
        doThrow(new RuntimeException("Producto no encontrado con ID: 999"))
                .when(productoService).eliminar(999L);

        mockMvc.perform(delete("/api/productos/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }
}