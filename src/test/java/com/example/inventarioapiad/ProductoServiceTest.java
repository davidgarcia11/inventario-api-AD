package com.example.inventarioapiad;

import com.example.inventarioapiad.entity.Producto;
import com.example.inventarioapiad.repository.ProductoRepository;
import com.example.inventarioapiad.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // TEST 1: Crear producto válido
    @Test
    public void testCrearProductoValido() {
        Producto producto = new Producto();
        producto.setNombre("Tornillo M10");
        producto.setSku("TORNILLO-M10");
        producto.setPrecioVenta(1.25f);
        producto.setStockTotal(100);

        Producto productoGuardado = new Producto();
        productoGuardado.setId(1L);
        productoGuardado.setNombre("Tornillo M10");

        when(productoRepository.save(any(Producto.class))).thenReturn(productoGuardado);

        Producto resultado = productoService.crear(producto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Tornillo M10", resultado.getNombre());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    // TEST 2: Crear producto sin nombre (400)
    @Test
    public void testCrearProductoSinNombre() {
        Producto producto = new Producto();
        producto.setNombre("");

        assertThrows(IllegalArgumentException.class, () -> productoService.crear(producto));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    // TEST 3: Crear producto sin SKU (400)
    @Test
    public void testCrearProductoSinSKU() {
        Producto producto = new Producto();
        producto.setNombre("Tornillo");
        producto.setSku("");

        assertThrows(IllegalArgumentException.class, () -> productoService.crear(producto));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    // TEST 4: Crear producto con precio negativo (400)
    @Test
    public void testCrearProductoConPrecioNegativo() {
        Producto producto = new Producto();
        producto.setNombre("Tornillo");
        producto.setSku("TORNILLO-M10");
        producto.setPrecioVenta(-1.0f);

        assertThrows(IllegalArgumentException.class, () -> productoService.crear(producto));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    // TEST 5: Buscar por ID existente (200)
    @Test
    public void testBuscarPorIdExistente() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Tornillo M10");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Producto resultado = productoService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(productoRepository, times(1)).findById(1L);
    }

    // TEST 6: Buscar por ID inexistente (404)
    @Test
    public void testBuscarPorIdInexistente() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productoService.buscarPorId(999L));
        verify(productoRepository, times(1)).findById(999L);
    }

    // TEST 7: Buscar todos
    @Test
    public void testBuscarTodos() {
        Producto p1 = new Producto(1L, "Tornillo", "SKU-1", null, 0.5f, 1.0f, 100, true, null);
        Producto p2 = new Producto(2L, "Tuerca", "SKU-2", null, 0.3f, 0.7f, 200, true, null);

        when(productoRepository.findAll()).thenReturn(java.util.Arrays.asList(p1, p2));

        java.util.List<Producto> resultado = productoService.buscarConFiltros(null, null, null);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    // TEST 8: Actualizar producto
    @Test
    public void testActualizarProducto() {
        Producto productoExistente = new Producto();
        productoExistente.setId(1L);
        productoExistente.setNombre("Tornillo M10");
        productoExistente.setPrecioVenta(1.0f);

        Producto actualizacion = new Producto();
        actualizacion.setNombre("Tornillo M12");
        actualizacion.setPrecioVenta(1.50f);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoExistente));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoExistente);

        Producto resultado = productoService.actualizar(1L, actualizacion);

        assertNotNull(resultado);
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    // TEST 9: Eliminar producto
    @Test
    public void testEliminarProducto() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setActivo(true);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        productoService.eliminar(1L);

        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(any(Producto.class));
    }
}