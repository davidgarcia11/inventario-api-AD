package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Cliente;
import com.example.inventarioapiad.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public Cliente crear(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        if (!cliente.getEmail().contains("@")) {
            throw new IllegalArgumentException("El email debe ser válido");
        }
        return clienteRepository.save(cliente);
    }

    public Cliente buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido");
        }
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
    }

    public List<Cliente> buscarTodos() {
        return (List<Cliente>) clienteRepository.findAll();
    }

    public Cliente actualizar(Long id, Cliente clienteActualizado) {
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

        return clienteRepository.save(cliente);
    }

    public void eliminar(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    // FILTRADO: Buscar clientes con hasta 3 campos
    public List<Cliente> buscarConFiltros(String nombre, String email, String ciudad) {
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

        return clientes;
    }
}