package com.example.inventarioapiad.dto;

import com.example.inventarioapiad.entity.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO para crear un Cliente en la V2 de la API. A diferencia de la V1,
// el email es OBLIGATORIO y se valida con @Email para garantizar formato
// correcto antes de tocar la BD. También aceptamos solo los campos que
// tiene sentido recibir al crear (no recibimos id, activo ni fechaCreacion).
@Data
public class ClienteCreateRequest {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    private String telefono;

    private String direccion;

    private String ciudad;

    // Conversión rápida del DTO a entidad: el resto de campos los
    // pone JPA (id) o ponemos por defecto (activo = true, fechaCreacion).
    public Cliente toEntity() {
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setEmail(email);
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        cliente.setCiudad(ciudad);
        return cliente;
    }
}
