package com.example.inventarioapiad.dto;

import com.example.inventarioapiad.entity.Almacen;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

// DTO para crear un Almacén en la V2.
//
// Diferencias con la V1:
//   - La V1 acepta la entidad Almacén entera (cualquier campo).
//   - La V2 acepta SOLO los campos que tienen sentido al crear, e
//     introduce el campo "prioritario" (boolean) que es nuevo de la V2.
//
// El campo prioritario es opcional: si no se envía, se asume false.
@Data
public class AlmacenCreateRequestV2 {

    @NotBlank(message = "El nombre del almacén es obligatorio")
    private String nombre;

    @NotBlank(message = "La ubicación es obligatoria")
    private String ubicacion;

    @Positive(message = "La capacidad máxima debe ser mayor a 0")
    private Integer capacidadMaxima;

    @Min(value = 0, message = "El stock actual no puede ser negativo")
    private Integer stockActual;

    private String responsable;

    // El campo "estrella" de la V2: marca si el almacén es prioritario.
    // Cuando es true, el DELETE de la V2 rechaza con 409.
    private Boolean prioritario = false;

    // Conversión rápida del DTO a entidad: el resto de campos los pone
    // JPA (id) o ponemos por defecto (activo=true, fechaCreacion=now).
    public Almacen toEntity() {
        Almacen almacen = new Almacen();
        almacen.setNombre(nombre);
        almacen.setUbicacion(ubicacion);
        almacen.setCapacidadMaxima(capacidadMaxima);
        if (stockActual != null) {
            almacen.setStockActual(stockActual);
        }
        almacen.setResponsable(responsable);
        almacen.setPrioritario(prioritario != null ? prioritario : false);
        return almacen;
    }
}
