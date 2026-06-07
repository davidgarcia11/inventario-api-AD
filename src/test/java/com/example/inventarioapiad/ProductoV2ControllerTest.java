package com.example.inventarioapiad;

import com.example.inventarioapiad.entity.Producto;
import com.example.inventarioapiad.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Tests del endpoint V2 paginado de productos.
// Verificamos los códigos HTTP y la estructura del PagedResponse.
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ProductoV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @Test
    public void listarPaginado_porDefecto_devuelve200() throws Exception {
        Producto p1 = new Producto();
        p1.setId(1L);
        p1.setNombre("Tornillo");
        p1.setSku("T1");
        p1.setPrecioVenta(1.0f);
        p1.setStockTotal(10);

        Page<Producto> pagina = new PageImpl<>(List.of(p1));
        when(productoService.buscarPaginado(any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/api/v2/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nombre").value("Tornillo"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    public void listarPaginado_conParametrosValidos_devuelve200() throws Exception {
        Page<Producto> pagina = new PageImpl<>(List.<Producto>of());
        when(productoService.buscarPaginado(any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/api/v2/productos")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "nombre,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    public void listarPaginado_pageNegativo_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v2/productos")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value(400));
    }

    @Test
    public void listarPaginado_sizeFueraDeRango_devuelve400() throws Exception {
        mockMvc.perform(get("/api/v2/productos")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v2/productos")
                        .param("size", "200"))
                .andExpect(status().isBadRequest());
    }
}
