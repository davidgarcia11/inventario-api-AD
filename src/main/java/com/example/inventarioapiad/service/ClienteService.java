package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Cliente;
import com.example.inventarioapiad.repository.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    @Autowired
    private ClienteRepository clienteRepository;

    public Cliente crear(Cliente cliente) {
        logger.info("Creando cliente: " + cliente.getNombre());

        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            logger.error("Error: Nombre del cliente vacío");
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            logger.error("Error: Email del cliente vacío");
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (!cliente.getEmail().contains("@")) {
            logger.error("Error: Email inválido");
            throw new IllegalArgumentException("El email debe ser válido");
        }

        Cliente creado = clienteRepository.save(cliente);
        logger.info("Cliente creado exitosamente con ID: " + creado.getId());
        return creado;
    }

    public Cliente buscarPorId(Long id) {
        logger.info("Buscando cliente con ID: " + id);

        if (id == null || id <= 0) {
            logger.error("Error: ID inválido");
            throw new IllegalArgumentException("El ID debe ser válido");
        }

        return clienteRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Cliente no encontrado con ID: " + id);
                    return new RuntimeException("Cliente no encontrado con ID: " + id);
                });
    }

    public List<Cliente> buscarTodos() {
        logger.info("Listando todos los clientes");
        return (List<Cliente>) clienteRepository.findAll();
    }

    public Cliente actualizar(Long id, Cliente clienteActualizado) {
        logger.info("Actualizando cliente con ID: " + id);

        Cliente cliente = buscarPorId(id);

        if (clienteActualizado.getNombre() != null && !clienteActualizado.getNombre().isBlank()) {
            cliente.setNombre(clienteActualizado.getNombre());
        }
        if (clienteActualizado.getEmail() != null && !clienteActualizado.getEmail().isBlank()) {
            cliente.setEmail(clienteActualizado.getEmail());
        }
        if (clienteActualizado.getTelefono() != null) {
            cliente.setTelefono(clienteActualizado.getTelefono());
        }
        if (clienteActualizado.getDireccion() != null) {
            cliente.setDireccion(clienteActualizado.getDireccion());
        }
        if (clienteActualizado.getCiudad() != null) {
            cliente.setCiudad(clienteActualizado.getCiudad());
        }
        if (clienteActualizado.getActivo() != null) {
            cliente.setActivo(clienteActualizado.getActivo());
        }

        Cliente actualizado = clienteRepository.save(cliente);
        logger.info("Cliente actualizado exitosamente con ID: " + id);
        return actualizado;
    }

    public void eliminar(Long id) {
        logger.info("Eliminando cliente con ID: " + id);

        Cliente cliente = buscarPorId(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);

        logger.info("Cliente eliminado (soft delete) con ID: " + id);
    }

    // FILTRADO: Buscar clientes con hasta 3 campos
    public List<Cliente> buscarConFiltros(String nombre, String email, String ciudad) {
        logger.info("Filtrando clientes - nombre: " + nombre + ", email: " + email + ", ciudad: " + ciudad);

        List<Cliente> clientes = ((List<Cliente>) clienteRepository.findAll()).stream()
                .filter(c -> Boolean.TRUE.equals(c.getActivo()))
                .collect(Collectors.toList());

        if (nombre != null && !nombre.isBlank()) {
            clientes = clientes.stream()
                    .filter(c -> c.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (email != null && !email.isBlank()) {
            clientes = clientes.stream()
                    .filter(c -> c.getEmail().toLowerCase().contains(email.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (ciudad != null && !ciudad.isBlank()) {
            clientes = clientes.stream()
                    .filter(c -> c.getCiudad().toLowerCase().contains(ciudad.toLowerCase()))
                    .collect(Collectors.toList());
        }

        logger.info("Filtrado completado. Resultados: " + clientes.size() + " clientes");
        return clientes;
    }
}