package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.entity.Compra;
import com.example.inventarioapiad.service.CompraService;
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
@RequestMapping("/api/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    // CREATE - POST /api/compras
    @PostMapping
    @Operation(summary = "Registrar Compra", description = "Crea un nuevo registro de compra. Campos obligatorios: proveedor, producto, almacen, cantidad, precioUnitario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compra registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (cantidad negativa, proveedor/producto no existe, etc.)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> crear(@RequestBody Compra compra) {
        try {
            Compra creada = compraService.crear(compra);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(400, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al crear la compra: " + e.getMessage())
            );
        }
    }

    // READ - GET /api/compras/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Compra por ID", description = "Obtiene los detalles de una compra específica por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compra encontrada"),
            @ApiResponse(responseCode = "404", description = "Compra no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Compra compra = compraService.buscarPorId(id);
            return ResponseEntity.ok(compra);
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
                    new ErrorResponse(500, "Error al buscar la compra: " + e.getMessage())
            );
        }
    }

    // READ ALL - GET /api/compras
    @GetMapping
    @Operation(summary = "Listar Compras", description = "Obtiene todas las compras registradas. Permite filtrar por estado, cantidad y número de factura.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de compras recuperada exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> buscarTodos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer cantidad,
            @RequestParam(required = false) String numeroFactura) {

        try {
            List<Compra> compras = compraService.buscarConFiltros(estado, cantidad, numeroFactura);
            return ResponseEntity.ok(compras);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al filtrar compras: " + e.getMessage())
            );
        }
    }

    // UPDATE - PUT /api/compras/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar Completo", description = "Actualiza todos los campos de la compra (reemplazo completo)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compra actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Compra no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Compra compraActualizada) {
        try {
            Compra actualizada = compraService.actualizar(id, compraActualizada);
            return ResponseEntity.ok(actualizada);
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
                    new ErrorResponse(500, "Error al actualizar la compra: " + e.getMessage())
            );
        }
    }

    // DELETE - DELETE /api/compras/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Compra", description = "Soft delete: Marca la compra como inactiva (activo=false) o cancelada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Compra eliminada (inactivada) correctamente"),
            @ApiResponse(responseCode = "404", description = "Compra no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            compraService.eliminar(id);
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
                    new ErrorResponse(500, "Error al eliminar la compra: " + e.getMessage())
            );
        }
    }

    // PATCH - Actualización parcial
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente una compra", description = "Actualiza solo los campos proporcionados (ej: cambiar estado, corregir cantidad).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compra actualizada parcialmente"),
            @ApiResponse(responseCode = "404", description = "Compra no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID de la compra", example = "1")
            @PathVariable Long id,
            @RequestBody Compra compraActualizada) {
        try {
            Compra existente = compraService.buscarPorId(id);

            if (compraActualizada.getCantidad() != null) {
                existente.setCantidad(compraActualizada.getCantidad());
            }
            if (compraActualizada.getPrecioUnitario() != null) {
                existente.setPrecioUnitario(compraActualizada.getPrecioUnitario());
            }
            if (compraActualizada.getNumeroFactura() != null) {
                existente.setNumeroFactura(compraActualizada.getNumeroFactura());
            }
            if (compraActualizada.getEstado() != null) {
                existente.setEstado(compraActualizada.getEstado());
            }

            Compra actualizada = compraService.actualizar(id, existente);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(404, "Compra no encontrada")
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