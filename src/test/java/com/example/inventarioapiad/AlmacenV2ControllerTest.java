package com.example.inventarioapiad;

import com.example.inventarioapiad.dto.AlmacenCreateRequestV2;
import com.example.inventarioapiad.dto.AlmacenUpdateRequestV2;
import com.example.inventarioapiad.entity.Almacen;
import com.example.inventarioapiad.service.AlmacenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Tests del AlmacenV2Controller, que reúne los 4 endpoints versionados
// alrededor del nuevo campo "prioritario".
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AlmacenV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlmacenService almacenService;

    // ---------- GET paginado ----------

    @Test
    public void getPaginado_devuelve200ConContenido() throws Exception {
        Almacen a = new Almacen();
        a.setId(1L);
        a.setNombre("Central");
        a.setUbicacion("Zaragoza");
        a.setCapacidadMaxima(10000);
        a.setPrioritario(true);

        Page<Almacen> pagina = new PageImpl<>(List.of(a));
        when(almacenService.buscarPaginado(any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/api/v2/almacenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nombre").value("Central"))
                .andExpect(jsonPath("$.content[0].prioritario").value(true))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    public void getPaginado_pageNegativo_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v2/almacenes").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    public void getPaginado_sizeFueraDeRango_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v2/almacenes").param("size", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v2/almacenes").param("size", "200"))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST crear ----------

    @Test
    public void crearAlmacen_valido_devuelve201YLocation() throws Exception {
        AlmacenCreateRequestV2 req = new AlmacenCreateRequestV2();
        req.setNombre("Almacén Central");
        req.setUbicacion("Zaragoza");
        req.setCapacidadMaxima(10000);
        req.setPrioritario(true);

        Almacen guardado = new Almacen();
        guardado.setId(42L);
        guardado.setNombre("Almacén Central");
        guardado.setUbicacion("Zaragoza");
        guardado.setCapacidadMaxima(10000);
        guardado.setPrioritario(true);

        when(almacenService.crear(any(Almacen.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/v2/almacenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/v2/almacenes/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.prioritario").value(true));
    }

    @Test
    public void crearAlmacen_sinNombre_devuelve400() throws Exception {
        AlmacenCreateRequestV2 req = new AlmacenCreateRequestV2();
        req.setUbicacion("Zaragoza");
        // sin nombre

        mockMvc.perform(post("/api/v2/almacenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void crearAlmacen_sinUbicacion_devuelve400() throws Exception {
        AlmacenCreateRequestV2 req = new AlmacenCreateRequestV2();
        req.setNombre("Almacén Central");
        // sin ubicación

        mockMvc.perform(post("/api/v2/almacenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT actualizar ----------

    @Test
    public void actualizarAlmacen_marcandoPrioritario_devuelve200() throws Exception {
        AlmacenUpdateRequestV2 req = new AlmacenUpdateRequestV2();
        req.setNombre("Central");
        req.setUbicacion("Zaragoza");
        req.setPrioritario(true);

        Almacen existente = new Almacen();
        existente.setId(1L);
        existente.setNombre("Antiguo");
        existente.setUbicacion("Madrid");
        existente.setPrioritario(false);

        Almacen actualizado = new Almacen();
        actualizado.setId(1L);
        actualizado.setNombre("Central");
        actualizado.setUbicacion("Zaragoza");
        actualizado.setPrioritario(true);

        when(almacenService.buscarPorId(eq(1L))).thenReturn(existente);
        when(almacenService.actualizar(eq(1L), any(Almacen.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v2/almacenes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Central"))
                .andExpect(jsonPath("$.prioritario").value(true));
    }

    @Test
    public void actualizarAlmacen_sinNombre_devuelve400() throws Exception {
        AlmacenUpdateRequestV2 req = new AlmacenUpdateRequestV2();
        req.setUbicacion("Zaragoza");
        // sin nombre

        mockMvc.perform(put("/api/v2/almacenes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void actualizarAlmacen_noExiste_devuelve404() throws Exception {
        AlmacenUpdateRequestV2 req = new AlmacenUpdateRequestV2();
        req.setNombre("Central");
        req.setUbicacion("Zaragoza");

        when(almacenService.buscarPorId(eq(999L)))
                .thenThrow(new RuntimeException("Almacén no encontrado con ID: 999"));

        mockMvc.perform(put("/api/v2/almacenes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    // ---------- DELETE con check de prioritario ----------

    @Test
    public void eliminarAlmacen_noPrioritario_devuelve204() throws Exception {
        doNothing().when(almacenService).eliminarSiNoPrioritario(1L);

        mockMvc.perform(delete("/api/v2/almacenes/1"))
                .andExpect(status().isNoContent());

        verify(almacenService).eliminarSiNoPrioritario(1L);
    }

    @Test
    public void eliminarAlmacen_prioritario_devuelve409() throws Exception {
        doThrow(new IllegalStateException(
                "No se puede eliminar un almacén prioritario. Marca prioritario=false antes de borrar."))
                .when(almacenService).eliminarSiNoPrioritario(1L);

        mockMvc.perform(delete("/api/v2/almacenes/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value(409))
                .andExpect(jsonPath("$.mensaje").value(
                        org.hamcrest.Matchers.containsString("prioritario")));
    }

    @Test
    public void eliminarAlmacen_noExiste_devuelve404() throws Exception {
        doThrow(new RuntimeException("Almacén no encontrado con ID: 999"))
                .when(almacenService).eliminarSiNoPrioritario(999L);

        mockMvc.perform(delete("/api/v2/almacenes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }
}
