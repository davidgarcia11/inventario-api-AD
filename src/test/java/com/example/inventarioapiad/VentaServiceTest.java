package com.example.inventarioapiad;

import com.example.inventarioapiad.entity.*;
import com.example.inventarioapiad.repository.VentaRepository;
import com.example.inventarioapiad.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private VentaService ventaService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCrearVentaValida() {
        Venta venta = new Venta();
        venta.setCliente(new Cliente(1L, "Cliente", "email@test.com", null, null, null, true, null));
        venta.setProducto(new Producto(1L, "Producto", "SKU", null, 0.5f, 1.0f, 100, true, null));
        venta.setAlmacen(new Almacen(1L, "Almacén", "Ubicación", 10000, 5000, null, true, null));
        venta.setCantidad(50);
        venta.setPrecioUnitario(1.50f);
        venta.setFechaVenta(LocalDateTime.now());
        venta.setNumeroPedido("PED-2025-0001");

        Venta guardada = new Venta();
        guardada.setId(1L);

        when(ventaRepository.save(any(Venta.class))).thenReturn(guardada);

        Venta resultado = ventaService.crear(venta);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    public void testCrearVentaSinCliente() {
        Venta venta = new Venta();
        venta.setCliente(null);

        assertThrows(IllegalArgumentException.class, () -> ventaService.crear(venta));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    public void testBuscarPorIdExistente() {
        Venta venta = new Venta();
        venta.setId(1L);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        Venta resultado = ventaService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    public void testBuscarPorIdInexistente() {
        when(ventaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> ventaService.buscarPorId(999L));
    }

    @Test
    public void testBuscarTodos() {
        when(ventaRepository.findAll()).thenReturn(java.util.Arrays.asList(
                new Venta(1L, null, null, null, 100, 1.0f, java.time.LocalDateTime.now(), "PED-1", "ENTREGADA", java.time.LocalDateTime.now()),
                new Venta(2L, null, null, null, 50, 1.5f, java.time.LocalDateTime.now(), "PED-2", "ENTREGADA", java.time.LocalDateTime.now())
        ));

        java.util.List<Venta> resultado = ventaService.buscarConFiltros(null, null, null);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    public void testActualizarVenta() {
        Venta existente = new Venta();
        existente.setId(1L);
        existente.setEstado("PENDIENTE");

        Venta actualizacion = new Venta();
        actualizacion.setEstado("ENTREGADA");

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(ventaRepository.save(any(Venta.class))).thenReturn(existente);

        Venta resultado = ventaService.actualizar(1L, actualizacion);

        assertNotNull(resultado);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    public void testEliminarVenta() {
        Venta venta = new Venta();
        venta.setId(1L);
        venta.setEstado("PENDIENTE");

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        ventaService.eliminar(1L);

        verify(ventaRepository, times(1)).save(any(Venta.class));
    }
}