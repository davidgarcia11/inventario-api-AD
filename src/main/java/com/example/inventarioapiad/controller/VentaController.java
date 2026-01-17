package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.entity.Venta;
import com.example.inventarioapiad.service.VentaService;
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
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    // CREATE - POST /api/ventas
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Venta venta) {
        try {
            Venta creada = ventaService.crear(venta);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(400, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al crear la venta: " + e.getMessage())
            );
        }
    }

    // READ - GET /api/ventas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Venta venta = ventaService.buscarPorId(id);
            return ResponseEntity.ok(venta);
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
                    new ErrorResponse(500, "Error al buscar la venta: " + e.getMessage())
            );
        }
    }

    // READ ALL - GET /api/ventas
    @GetMapping
    public ResponseEntity<?> buscarTodos(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer cantidad,
            @RequestParam(required = false) String numeroPedido) {

        try {
            List<Venta> ventas = ventaService.buscarConFiltros(estado, cantidad, numeroPedido);
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al filtrar ventas: " + e.getMessage())
            );
        }
    }

    // UPDATE - PUT /api/ventas/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Venta ventaActualizada) {
        try {
            Venta actualizada = ventaService.actualizar(id, ventaActualizada);
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
                    new ErrorResponse(500, "Error al actualizar la venta: " + e.getMessage())
            );
        }
    }

    // DELETE - DELETE /api/ventas/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            ventaService.eliminar(id);
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
                    new ErrorResponse(500, "Error al eliminar la venta: " + e.getMessage())
            );
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente una venta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta actualizada parcialmente"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID de la venta", example = "1")
            @PathVariable Long id,
            @RequestBody Venta ventaActualizada) {
        try {
            Venta existente = ventaService.buscarPorId(id);

            if (ventaActualizada.getCantidad() != null) {
                existente.setCantidad(ventaActualizada.getCantidad());
            }
            if (ventaActualizada.getPrecioUnitario() != null) {
                existente.setPrecioUnitario(ventaActualizada.getPrecioUnitario());
            }
            if (ventaActualizada.getNumeroPedido() != null) {
                existente.setNumeroPedido(ventaActualizada.getNumeroPedido());
            }
            if (ventaActualizada.getEstado() != null) {
                existente.setEstado(ventaActualizada.getEstado());
            }

            Venta actualizada = ventaService.actualizar(id, existente);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(404, "Venta no encontrada")
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