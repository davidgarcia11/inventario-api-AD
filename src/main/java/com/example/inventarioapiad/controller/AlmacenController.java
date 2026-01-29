package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.entity.Almacen;
import com.example.inventarioapiad.service.AlmacenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@RestController
@RequestMapping("/api/almacenes")
public class AlmacenController {

    @Autowired
    private AlmacenService almacenService;

    // CREATE - POST /api/almacenes
    @PostMapping
    @Operation(summary = "Crear Almacén", description = "Crea un nuevo almacén. Campos obligatorios: nombre, ubicacion.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Almacén creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre vacío, capacidad negativa, etc.)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> crear(@RequestBody Almacen almacen) {
        try {
            Almacen creado = almacenService.crear(almacen);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(400, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al crear el almacén: " + e.getMessage())
            );
        }
    }

    // READ - GET /api/almacenes/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Almacén por ID", description = "Obtiene un almacén específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Almacén encontrado"),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Almacen almacen = almacenService.buscarPorId(id);
            return ResponseEntity.ok(almacen);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(400, e.getMessage())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(404, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al buscar el almacén: " + e.getMessage())
            );
        }
    }

    // READ ALL - GET /api/almacenes
    @GetMapping
    @Operation(summary = "Listar Almacenes", description = "Obtiene todos los almacenes activos. Permite filtrar por nombre, ubicación y capacidad máxima.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de almacenes recuperada exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> buscarTodos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) Integer capacidadMaxima) {

        try {
            List<Almacen> almacenes = almacenService.buscarConFiltros(nombre, ubicacion, capacidadMaxima);
            return ResponseEntity.ok(almacenes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al filtrar almacenes: " + e.getMessage())
            );
        }
    }

    // UPDATE - PUT /api/almacenes/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar Completo", description = "Actualiza todos los campos del almacén (reemplazo completo)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Almacén actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Almacen almacenActualizado) {
        try {
            Almacen actualizado = almacenService.actualizar(id, almacenActualizado);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(400, e.getMessage())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(404, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al actualizar el almacén: " + e.getMessage())
            );
        }
    }

    // DELETE - DELETE /api/almacenes/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Almacén", description = "Soft delete: marca el almacén como inactivo (activo=false)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Almacén eliminado (inactivado) correctamente"),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            almacenService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(400, e.getMessage())
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(404, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al eliminar el almacén: " + e.getMessage())
            );
        }
    }

    // PATCH - Actualización parcial
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente un almacén", description = "Actualiza solo los campos proporcionados, manteniendo el resto igual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Almacén actualizado parcialmente"),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID del almacén", example = "1")
            @PathVariable Long id,
            @RequestBody Almacen almacenActualizado) {
        try {
            Almacen existente = almacenService.buscarPorId(id);

            if (almacenActualizado.getNombre() != null) {
                existente.setNombre(almacenActualizado.getNombre());
            }
            if (almacenActualizado.getUbicacion() != null) {
                existente.setUbicacion(almacenActualizado.getUbicacion());
            }
            if (almacenActualizado.getCapacidadMaxima() != null) {
                existente.setCapacidadMaxima(almacenActualizado.getCapacidadMaxima());
            }
            if (almacenActualizado.getStockActual() != null) {
                existente.setStockActual(almacenActualizado.getStockActual());
            }
            if (almacenActualizado.getResponsable() != null) {
                existente.setResponsable(almacenActualizado.getResponsable());
            }

            Almacen actualizado = almacenService.actualizar(id, existente);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(404, "Almacén no encontrado")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al actualizar: " + e.getMessage())
            );
        }
    }

    public static class ErrorResponse {
        public int codigo;
        public String mensaje;

        public ErrorResponse(int codigo, String mensaje) {
            this.codigo = codigo;
            this.mensaje = mensaje;
        }

        public int getCodigo() { return codigo; }
        public String getMensaje() { return mensaje; }
    }
}