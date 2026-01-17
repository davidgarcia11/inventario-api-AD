package com.example.inventarioapiad;

import com.example.inventarioapiad.entity.Cliente;
import com.example.inventarioapiad.repository.ClienteRepository;
import com.example.inventarioapiad.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCrearClienteValido() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Constructora López");
        cliente.setEmail("ventas@constructoralopez.com");

        Cliente guardado = new Cliente();
        guardado.setId(1L);
        guardado.setNombre("Constructora López");

        when(clienteRepository.save(any(Cliente.class))).thenReturn(guardado);

        Cliente resultado = clienteService.crear(cliente);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    public void testCrearClienteSinNombre() {
        Cliente cliente = new Cliente();
        cliente.setNombre("");

        assertThrows(IllegalArgumentException.class, () -> clienteService.crear(cliente));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    public void testBuscarPorIdExistente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Constructora López");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    public void testBuscarPorIdInexistente() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> clienteService.buscarPorId(999L));
    }

    @Test
    public void testBuscarTodos() {
        when(clienteRepository.findAll()).thenReturn(java.util.Arrays.asList(
                new Cliente(1L, "Cliente 1", "email1@test.com", "123", "Dirección 1", "Ciudad 1", true, null),
                new Cliente(2L, "Cliente 2", "email2@test.com", "456", "Dirección 2", "Ciudad 2", true, null)
        ));

        java.util.List<Cliente> resultado = clienteService.buscarConFiltros(null, null, null);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
    }

    @Test
    public void testActualizarCliente() {
        Cliente existente = new Cliente();
        existente.setId(1L);
        existente.setNombre("Constructora López");

        Cliente actualizacion = new Cliente();
        actualizacion.setNombre("Constructora García");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(existente);

        Cliente resultado = clienteService.actualizar(1L, actualizacion);

        assertNotNull(resultado);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    public void testEliminarCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        clienteService.eliminar(1L);

        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }
}