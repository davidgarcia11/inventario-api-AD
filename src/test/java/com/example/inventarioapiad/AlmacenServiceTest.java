package com.example.inventarioapiad;

import com.example.inventarioapiad.entity.Almacen;
import com.example.inventarioapiad.repository.AlmacenRepository;
import com.example.inventarioapiad.service.AlmacenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AlmacenServiceTest {

    @Mock
    private AlmacenRepository almacenRepository;

    @InjectMocks
    private AlmacenService almacenService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCrearAlmacenValido() {
        Almacen almacen = new Almacen();
        almacen.setNombre("Almacén Central");
        almacen.setUbicacion("Barcelona");
        almacen.setCapacidadMaxima(10000);

        Almacen almacenGuardado = new Almacen();
        almacenGuardado.setId(1L);
        almacenGuardado.setNombre("Almacén Central");

        when(almacenRepository.save(any(Almacen.class))).thenReturn(almacenGuardado);

        Almacen resultado = almacenService.crear(almacen);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(almacenRepository, times(1)).save(any(Almacen.class));
    }

    @Test
    public void testCrearAlmacenSinNombre() {
        Almacen almacen = new Almacen();
        almacen.setNombre("");

        assertThrows(IllegalArgumentException.class, () -> almacenService.crear(almacen));
        verify(almacenRepository, never()).save(any(Almacen.class));
    }

    @Test
    public void testBuscarPorIdExistente() {
        Almacen almacen = new Almacen();
        almacen.setId(1L);
        almacen.setNombre("Almacén Central");

        when(almacenRepository.findById(1L)).thenReturn(Optional.of(almacen));

        Almacen resultado = almacenService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(almacenRepository, times(1)).findById(1L);
    }

    @Test
    public void testBuscarPorIdInexistente() {
        when(almacenRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> almacenService.buscarPorId(999L));
    }

    @Test
    public void testBuscarTodos() {
        when(almacenRepository.findAll()).thenReturn(java.util.Arrays.asList(
                new Almacen(1L, "Almacén 1", "Ubicación 1", 10000, 5000, "Responsable 1", true, null),
                new Almacen(2L, "Almacén 2", "Ubicación 2", 8000, 3000, "Responsable 2", true, null)
        ));

        java.util.List<Almacen> resultado = almacenService.buscarConFiltros(null, null, null);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    public void testActualizarAlmacen() {
        Almacen existente = new Almacen();
        existente.setId(1L);
        existente.setNombre("Almacén Central");

        Almacen actualizacion = new Almacen();
        actualizacion.setNombre("Almacén Madrid");

        when(almacenRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(almacenRepository.save(any(Almacen.class))).thenReturn(existente);

        Almacen resultado = almacenService.actualizar(1L, actualizacion);

        assertNotNull(resultado);
        verify(almacenRepository, times(1)).save(any(Almacen.class));
    }

    @Test
    public void testEliminarAlmacen() {
        Almacen almacen = new Almacen();
        almacen.setId(1L);

        when(almacenRepository.findById(1L)).thenReturn(Optional.of(almacen));
        when(almacenRepository.save(any(Almacen.class))).thenReturn(almacen);

        almacenService.eliminar(1L);

        verify(almacenRepository, times(1)).save(any(Almacen.class));
    }
}