package com.example.inventarioapiad.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO para actualizar un Almacén en la V2.
// Solo permite cambiar nombre, ubicación y stockActual. La capacidad
// máxima es una propiedad física del almacén y ya no se cambia por la
// API operativa (cambia el modelo de negocio de la V1, que sí lo permitía).
@Data
public class AlmacenUpdateRequestV2 {

    @NotBlank(message = "El nombre del almacén es obligatorio")
    private String nombre;

    @NotBlank(message = "La ubicación es obligatoria")
    private String ubicacion;

    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer stockActual;
}
