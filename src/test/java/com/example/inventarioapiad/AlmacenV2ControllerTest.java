package com.example.inventarioapiad;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Tests del endpoint V2 PUT de almacenes (con DTO restringido).
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AlmacenV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlmacenService almacenService;

    @Test
    public void actualizarAlmacen_valido_devuelve200() throws Exception {
        AlmacenUpdateRequestV2 req = new AlmacenUpdateRequestV2();
        req.setNombre("Almacén Central");
        req.setUbicacion("Zaragoza");
        req.setStockActual(50);

        Almacen existente = new Almacen();
        existente.setId(1L);
        existente.setNombre("Antiguo");
        existente.setUbicacion("Madrid");
        existente.setCapacidadMaxima(100);

        Almacen actualizado = new Almacen();
        actualizado.setId(1L);
        actualizado.setNombre("Almacén Central");
        actualizado.setUbicacion("Zaragoza");
        actualizado.setStockActual(50);

        when(almacenService.buscarPorId(eq(1L))).thenReturn(existente);
        when(almacenService.actualizar(eq(1L), any(Almacen.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v2/almacenes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Almacén Central"))
                .andExpect(jsonPath("$.ubicacion").value("Zaragoza"));
    }

    @Test
    public void actualizarAlmacen_sinNombre_devuelve400() throws Exception {
        AlmacenUpdateRequestV2 req = new AlmacenUpdateRequestV2();
        req.setUbicacion("Zaragoza");
        // nombre vacío

        mockMvc.perform(put("/api/v2/almacenes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void actualizarAlmacen_noExiste_devuelve404() throws Exception {
        AlmacenUpdateRequestV2 req = new AlmacenUpdateRequestV2();
        req.setNombre("Test");
        req.setUbicacion("Test");

        when(almacenService.buscarPorId(eq(999L)))
                .thenThrow(new RuntimeException("Almacén no encontrado con ID: 999"));

        mockMvc.perform(put("/api/v2/almacenes/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }
}
