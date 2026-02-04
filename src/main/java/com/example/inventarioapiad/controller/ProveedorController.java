package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.entity.Proveedor;
import com.example.inventarioapiad.service.ProveedorService;
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

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    // CREATE - POST /api/proveedores
    @PostMapping
    @Operation(summary = "Crear Proveedor", description = "Crea un nuevo proveedor. Campos obligatorios: nombre, email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Proveedor creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre vacío, email incorrecto, etc.)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> crear(@RequestBody Proveedor proveedor) {
        try {
            Proveedor creado = proveedorService.crear(proveedor);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(400, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al crear el proveedor: " + e.getMessage())
            );
        }
    }

    // READ - GET /api/proveedores/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Proveedor por ID", description = "Obtiene un proveedor específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Proveedor proveedor = proveedorService.buscarPorId(id);
            return ResponseEntity.ok(proveedor);
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
                    new ErrorResponse(500, "Error al buscar el proveedor: " + e.getMessage())
            );
        }
    }

    // READ ALL - GET /api/proveedores
    @GetMapping
    @Operation(summary = "Listar Proveedores", description = "Obtiene todos los proveedores activos. Permite filtrar por nombre, email y días de entrega.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de proveedores recuperada exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> buscarTodos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer diasEntrega) {

        try {
            List<Proveedor> proveedores = proveedorService.buscarConFiltros(nombre, email, diasEntrega);
            return ResponseEntity.ok(proveedores);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al filtrar proveedores: " + e.getMessage())
            );
        }
    }

    // UPDATE - PUT /api/proveedores/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar Completo", description = "Actualiza todos los campos del proveedor (reemplazo completo)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Proveedor proveedorActualizado) {
        try {
            Proveedor actualizado = proveedorService.actualizar(id, proveedorActualizado);
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
                    new ErrorResponse(500, "Error al actualizar el proveedor: " + e.getMessage())
            );
        }
    }

    // DELETE - DELETE /api/proveedores/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Proveedor", description = "Soft delete: marca el proveedor como inactivo (activo=false)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Proveedor eliminado (inactivado) correctamente"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            proveedorService.eliminar(id);
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
                    new ErrorResponse(500, "Error al eliminar el proveedor: " + e.getMessage())
            );
        }
    }

    // PATCH - Actualización parcial
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente un proveedor", description = "Actualiza solo los campos proporcionados, manteniendo el resto igual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor actualizado parcialmente"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID del proveedor", example = "1")
            @PathVariable Long id,
            @RequestBody Proveedor proveedorActualizado) {
        try {
            Proveedor existente = proveedorService.buscarPorId(id);

            if (proveedorActualizado.getNombre() != null) {
                existente.setNombre(proveedorActualizado.getNombre());
            }
            if (proveedorActualizado.getEmail() != null) {
                existente.setEmail(proveedorActualizado.getEmail());
            }
            if (proveedorActualizado.getTelefono() != null) {
                existente.setTelefono(proveedorActualizado.getTelefono());
            }
            if (proveedorActualizado.getPais() != null) {
                existente.setPais(proveedorActualizado.getPais());
            }
            if (proveedorActualizado.getDiasEntrega() != null) {
                existente.setDiasEntrega(proveedorActualizado.getDiasEntrega());
            }

            Proveedor actualizado = proveedorService.actualizar(id, existente);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(404, "Proveedor no encontrado")
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
