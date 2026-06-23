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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Tests del AlmacenV2Controller (V2 con paridad respecto a V1 + el
// campo "prioritario"). Cubre los 6 endpoints (POST, GET id, GET lista
// con filtro prioritario, PUT, PATCH, DELETE) y los códigos clave:
// 200/201/204 cuando va bien, 400 con datos inválidos, 404 cuando no
// existe y 409 cuando el almacén es prioritario y se intenta borrar.
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AlmacenV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlmacenService almacenService;

    // ---------- POST crear ----------

    @Test
    public void crearAlmacen_valido_devuelve201ConPrioritario() throws Exception {
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

    // ---------- GET por id ----------

    @Test
    public void buscarPorId_existente_devuelve200ConPrioritario() throws Exception {
        Almacen a = new Almacen();
        a.setId(7L);
        a.setNombre("Norte");
        a.setUbicacion("Bilbao");
        a.setPrioritario(false);

        when(almacenService.buscarPorId(7L)).thenReturn(a);

        mockMvc.perform(get("/api/v2/almacenes/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.prioritario").value(false));
    }

    @Test
    public void buscarPorId_noExiste_devuelve404() throws Exception {
        when(almacenService.buscarPorId(999L))
                .thenThrow(new RuntimeException("Almacén no encontrado con ID: 999"));

        mockMvc.perform(get("/api/v2/almacenes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    // ---------- GET lista ----------

    @Test
    public void buscarTodos_devuelveListaConPrioritario() throws Exception {
        Almacen a = new Almacen();
        a.setId(1L);
        a.setNombre("Central");
        a.setPrioritario(true);

        when(almacenService.buscarConFiltrosV2(null, null, null, null))
                .thenReturn(List.of(a));

        mockMvc.perform(get("/api/v2/almacenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombre").value("Central"))
                .andExpect(jsonPath("$[0].prioritario").value(true));
    }

    @Test
    public void buscarTodos_conFiltroPrioritario_pasaFiltroAlService() throws Exception {
        when(almacenService.buscarConFiltrosV2(null, null, null, true))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v2/almacenes").param("prioritario", "true"))
                .andExpect(status().isOk());

        verify(almacenService).buscarConFiltrosV2(null, null, null, true);
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

    // ---------- PATCH parcial ----------

    @Test
    public void patchSoloPrioritario_devuelve200() throws Exception {
        // Body parcial: solo cambia prioritario, el resto se mantiene.
        String body = "{\"prioritario\": false}";

        Almacen existente = new Almacen();
        existente.setId(1L);
        existente.setNombre("Central");
        existente.setUbicacion("Zaragoza");
        existente.setPrioritario(true);

        Almacen actualizado = new Almacen();
        actualizado.setId(1L);
        actualizado.setNombre("Central");
        actualizado.setUbicacion("Zaragoza");
        actualizado.setPrioritario(false);

        when(almacenService.buscarPorId(eq(1L))).thenReturn(existente);
        when(almacenService.actualizar(eq(1L), any(Almacen.class))).thenReturn(actualizado);

        mockMvc.perform(patch("/api/v2/almacenes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prioritario").value(false))
                .andExpect(jsonPath("$.nombre").value("Central"));
    }

    @Test
    public void patchNoExiste_devuelve404() throws Exception {
        String body = "{\"prioritario\": false}";

        when(almacenService.buscarPorId(eq(999L)))
                .thenThrow(new RuntimeException("Almacén no encontrado con ID: 999"));

        mockMvc.perform(patch("/api/v2/almacenes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
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
