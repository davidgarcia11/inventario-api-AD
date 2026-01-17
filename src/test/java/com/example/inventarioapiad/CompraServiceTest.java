package com.example.inventarioapiad;

import com.example.inventarioapiad.entity.*;
import com.example.inventarioapiad.repository.CompraRepository;
import com.example.inventarioapiad.service.CompraService;
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

public class CompraServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @InjectMocks
    private CompraService compraService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCrearCompraValida() {
        Compra compra = new Compra();
        compra.setProveedor(new Proveedor(1L, "Proveedor", "email@test.com", null, null, null, true, null));
        compra.setProducto(new Producto(1L, "Producto", "SKU", null, 0.5f, 1.0f, 100, true, null));
        compra.setAlmacen(new Almacen(1L, "Almacén", "Ubicación", 10000, 5000, null, true, null));
        compra.setCantidad(100);
        compra.setPrecioUnitario(1.0f);
        compra.setFechaCompra(LocalDateTime.now());

        Compra guardada = new Compra();
        guardada.setId(1L);

        when(compraRepository.save(any(Compra.class))).thenReturn(guardada);

        Compra resultado = compraService.crear(compra);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(compraRepository, times(1)).save(any(Compra.class));
    }

    @Test
    public void testCrearCompraSinProveedor() {
        Compra compra = new Compra();
        compra.setProveedor(null);

        assertThrows(IllegalArgumentException.class, () -> compraService.crear(compra));
        verify(compraRepository, never()).save(any(Compra.class));
    }

    @Test
    public void testBuscarPorIdExistente() {
        Compra compra = new Compra();
        compra.setId(1L);

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));

        Compra resultado = compraService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    public void testBuscarPorIdInexistente() {
        when(compraRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> compraService.buscarPorId(999L));
    }

    @Test
    public void testBuscarTodos() {
        when(compraRepository.findAll()).thenReturn(java.util.Arrays.asList(
                new Compra(1L, null, null, null, 100, 1.0f, LocalDateTime.now(), "FAC-1", "RECIBIDA", LocalDateTime.now()),
                new Compra(2L, null, null, null, 200, 1.5f, LocalDateTime.now(), "FAC-2", "RECIBIDA", LocalDateTime.now())
        ));

        java.util.List<Compra> resultado = compraService.buscarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    public void testActualizarCompra() {
        Compra existente = new Compra();
        existente.setId(1L);
        existente.setEstado("PENDIENTE");

        Compra actualizacion = new Compra();
        actualizacion.setEstado("RECIBIDA");

        when(compraRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(compraRepository.save(any(Compra.class))).thenReturn(existente);

        Compra resultado = compraService.actualizar(1L, actualizacion);

        assertNotNull(resultado);
        verify(compraRepository, times(1)).save(any(Compra.class));
    }

    @Test
    public void testEliminarCompra() {
        Compra compra = new Compra();
        compra.setId(1L);
        compra.setEstado("PENDIENTE");

        when(compraRepository.findById(1L)).thenReturn(Optional.of(compra));
        when(compraRepository.save(any(Compra.class))).thenReturn(compra);

        compraService.eliminar(1L);

        verify(compraRepository, times(1)).save(any(Compra.class));
    }
}