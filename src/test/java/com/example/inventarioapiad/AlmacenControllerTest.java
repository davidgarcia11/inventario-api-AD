package com.example.inventarioapiad;

import com.example.inventarioapiad.entity.Almacen;
import com.example.inventarioapiad.service.AlmacenService;
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
public class AlmacenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlmacenService almacenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCrearAlmacen201() throws Exception {
        Almacen almacen = new Almacen();
        almacen.setNombre("Almacén Central");
        almacen.setUbicacion("Barcelona");

        Almacen almacenCreado = new Almacen();
        almacenCreado.setId(1L);
        almacenCreado.setNombre("Almacén Central");

        when(almacenService.crear(any(Almacen.class))).thenReturn(almacenCreado);

        mockMvc.perform(post("/api/almacenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(almacen)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCrearAlmacen400() throws Exception {
        Almacen almacen = new Almacen();
        almacen.setNombre("");

        doThrow(new IllegalArgumentException("El nombre del almacén es obligatorio"))
                .when(almacenService).crear(any(Almacen.class));

        mockMvc.perform(post("/api/almacenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(almacen)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    public void testBuscarPorId200() throws Exception {
        Almacen almacen = new Almacen();
        almacen.setId(1L);
        almacen.setNombre("Almacén Central");

        when(almacenService.buscarPorId(1L)).thenReturn(almacen);

        mockMvc.perform(get("/api/almacenes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testBuscarPorId404() throws Exception {
        doThrow(new RuntimeException("Almacén no encontrado con ID: 999"))
                .when(almacenService).buscarPorId(999L);

        mockMvc.perform(get("/api/almacenes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testBuscarTodos200() throws Exception {
        mockMvc.perform(get("/api/almacenes"))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizar200() throws Exception {
        Almacen almacenActualizado = new Almacen();
        almacenActualizado.setNombre("Almacén Madrid");

        Almacen result = new Almacen();
        result.setId(1L);
        result.setNombre("Almacén Madrid");

        when(almacenService.actualizar(eq(1L), any(Almacen.class)))
                .thenReturn(result);

        mockMvc.perform(put("/api/almacenes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(almacenActualizado)))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizar404() throws Exception {
        Almacen almacenActualizado = new Almacen();

        doThrow(new RuntimeException("Almacén no encontrado con ID: 999"))
                .when(almacenService).actualizar(eq(999L), any(Almacen.class));

        mockMvc.perform(put("/api/almacenes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(almacenActualizado)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminar204() throws Exception {
        mockMvc.perform(delete("/api/almacenes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testEliminar404() throws Exception {
        doThrow(new RuntimeException("Almacén no encontrado con ID: 999"))
                .when(almacenService).eliminar(999L);

        mockMvc.perform(delete("/api/almacenes/999"))
                .andExpect(status().isNotFound());
    }
}