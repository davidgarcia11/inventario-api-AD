package com.example.inventarioapiad;

import com.example.inventarioapiad.entity.Proveedor;
import com.example.inventarioapiad.repository.ProveedorRepository;
import com.example.inventarioapiad.service.ProveedorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCrearProveedorValido() {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Aceros García");
        proveedor.setEmail("contacto@acerosgarcia.com");

        Proveedor guardado = new Proveedor();
        guardado.setId(1L);
        guardado.setNombre("Aceros García");

        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(guardado);

        Proveedor resultado = proveedorService.crear(proveedor);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(proveedorRepository, times(1)).save(any(Proveedor.class));
    }

    @Test
    public void testCrearProveedorSinNombre() {
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("");

        assertThrows(IllegalArgumentException.class, () -> proveedorService.crear(proveedor));
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    public void testBuscarPorIdExistente() {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setNombre("Aceros García");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        Proveedor resultado = proveedorService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    public void testBuscarPorIdInexistente() {
        when(proveedorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> proveedorService.buscarPorId(999L));
    }

    @Test
    public void testBuscarTodos() {
        when(proveedorRepository.findAll()).thenReturn(java.util.Arrays.asList(
                new Proveedor(1L, "Proveedor 1", "email1@test.com", "123", "España", 3, true, null),
                new Proveedor(2L, "Proveedor 2", "email2@test.com", "456", "España", 2, true, null)
        ));

        java.util.List<Proveedor> resultado = proveedorService.buscarConFiltros(null, null, null);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    public void testActualizarProveedor() {
        Proveedor existente = new Proveedor();
        existente.setId(1L);
        existente.setNombre("Aceros García");

        Proveedor actualizacion = new Proveedor();
        actualizacion.setNombre("Aceros López");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(existente);

        Proveedor resultado = proveedorService.actualizar(1L, actualizacion);

        assertNotNull(resultado);
        verify(proveedorRepository, times(1)).save(any(Proveedor.class));
    }

    @Test
    public void testEliminarProveedor() {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedor);

        proveedorService.eliminar(1L);

        verify(proveedorRepository, times(1)).save(any(Proveedor.class));
    }
}