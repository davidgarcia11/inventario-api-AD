package com.example.inventarioapiad.controller.v2;

import com.example.inventarioapiad.dto.AlmacenUpdateRequestV2;
import com.example.inventarioapiad.entity.Almacen;
import com.example.inventarioapiad.service.AlmacenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ----------------------------------------------------------------------
// Endpoint V2 de Almacenes
// ----------------------------------------------------------------------
// Cambio respecto a la V1:
//   La V1 (PUT /api/almacenes/{id}) recibe la entidad Almacen entera y
//   permite modificar cualquier campo, incluyendo la capacidad máxima.
//
//   La V2 (PUT /api/v2/almacenes/{id}) recibe un DTO restringido
//   (AlmacenUpdateRequestV2) con SOLO los campos que tiene sentido
//   cambiar a nivel operativo: nombre, ubicación y stockActual.
//   La capacidad máxima es una propiedad física del almacén, no algo
//   que se modifique día a día desde una API.
// ----------------------------------------------------------------------
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v2/almacenes")
public class AlmacenV2Controller {

    @Autowired
    private AlmacenService almacenService;

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar almacén (V2)",
               description = "Actualiza solo nombre, ubicación y stockActual de un almacén.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Almacén actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody AlmacenUpdateRequestV2 request) {
        try {
            // Buscamos el almacén existente para que tire 404 si no está
            Almacen existente = almacenService.buscarPorId(id);

            // Aplicamos solo los campos permitidos por el DTO
            existente.setNombre(request.getNombre());
            existente.setUbicacion(request.getUbicacion());
            if (request.getStockActual() != null) {
                existente.setStockActual(request.getStockActual());
            }

            Almacen actualizado = almacenService.actualizar(id, existente);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponseV2(400, e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponseV2(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponseV2(500, "Error al actualizar el almacén: " + e.getMessage()));
        }
    }
}
