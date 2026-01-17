package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Cliente;
import com.example.inventarioapiad.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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
}