package com.example.inventarioapiad.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO para actualizar un Almacén en la V2 (PUT).
//
// A diferencia de la V1, este DTO permite cambiar el campo "prioritario",
// que es el campo "estrella" introducido en la V2. La capacidad máxima
// sigue siendo NO modificable desde la V2 (es una propiedad física del
// almacén) - eso refuerza el ejemplo del versionado: la V2 marca
// claramente qué se puede tocar y qué no.
@Data
public class AlmacenUpdateRequestV2 {

    @NotBlank(message = "El nombre del almacén es obligatorio")
    private String nombre;

    @NotBlank(message = "La ubicación es obligatoria")
    private String ubicacion;

    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer stockActual;

    private String responsable;

    // Permite cambiar el estado prioritario del almacén.
    // Si lo activas, el DELETE V2 dejará de funcionar hasta volver a
    // ponerlo en false.
    private Boolean prioritario;
}
