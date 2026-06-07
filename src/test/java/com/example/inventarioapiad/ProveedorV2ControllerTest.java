package com.example.inventarioapiad;

import com.example.inventarioapiad.service.ProveedorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Tests del endpoint V2 DELETE de proveedores (con soft/hard delete).
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ProveedorV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProveedorService proveedorService;

    @Test
    public void eliminarProveedor_softPorDefecto_devuelve204() throws Exception {
        doNothing().when(proveedorService).eliminar(1L);

        mockMvc.perform(delete("/api/v2/proveedores/1"))
                .andExpect(status().isNoContent());

        verify(proveedorService).eliminar(1L);
        verify(proveedorService, never()).eliminarFisico(anyLong());
    }

    @Test
    public void eliminarProveedor_hardTrue_devuelve204YLlamaBorradoFisico() throws Exception {
        doNothing().when(proveedorService).eliminarFisico(1L);

        mockMvc.perform(delete("/api/v2/proveedores/1")
                        .param("hard", "true"))
                .andExpect(status().isNoContent());

        verify(proveedorService).eliminarFisico(1L);
        verify(proveedorService, never()).eliminar(anyLong());
    }

    @Test
    public void eliminarProveedor_noExiste_devuelve404() throws Exception {
        doThrow(new RuntimeException("Proveedor no encontrado con ID: 999"))
                .when(proveedorService).eliminar(999L);

        mockMvc.perform(delete("/api/v2/proveedores/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }
}
