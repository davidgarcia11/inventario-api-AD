package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.entity.Producto;
import com.example.inventarioapiad.service.ProductoService;
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
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // CREATE - POST /api/productos
    @PostMapping
    @Operation(summary = "Crear Producto", description = "Crea un nuevo producto. Campos obligatorios: nombre, sku, precioVenta, stockTotal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre vacío, precio negativo, etc.)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> crear(@RequestBody Producto producto) {
        try {
            Producto creado = productoService.crear(producto);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(400, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al crear el producto: " + e.getMessage())
            );
        }
    }

    // READ - GET /api/productos/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Producto por ID", description = "Obtiene un producto específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Producto producto = productoService.buscarPorId(id);
            return ResponseEntity.ok(producto);
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
                    new ErrorResponse(500, "Error al buscar el producto: " + e.getMessage())
            );
        }
    }

    // READ ALL - GET /api/productos
    @GetMapping
    @Operation(summary = "Listar Productos", description = "Obtiene todos los productos activos. Permite filtrar por nombre, sku y precioVenta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos recuperada exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> buscarTodos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) Float precioVenta) {

        try {
            List<Producto> productos = productoService.buscarConFiltros(nombre, sku, precioVenta);
            return ResponseEntity.ok(productos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al filtrar productos: " + e.getMessage())
            );
        }
    }

    // UPDATE - PUT /api/productos/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar Completo", description = "Actualiza todos los campos del producto (reemplazo completo)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Producto productoActualizado) {
        try {
            Producto actualizado = productoService.actualizar(id, productoActualizado);
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
                    new ErrorResponse(500, "Error al actualizar el producto: " + e.getMessage())
            );
        }
    }

    // DELETE - DELETE /api/productos/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Producto", description = "Soft delete: marca el producto como inactivo (activo=false)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado (inactivado) correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            productoService.eliminar(id);
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
                    new ErrorResponse(500, "Error al eliminar el producto: " + e.getMessage())
            );
        }
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente un producto", description = "Actualiza solo los campos proporcionados, sin cambiar los demás")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado parcialmente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID del producto", example = "1")
            @PathVariable Long id,
            @RequestBody Producto productoActualizado) {
        try {
            Producto existente = productoService.buscarPorId(id);

            // Actualizar solo los campos que vinieron (no null)
            if (productoActualizado.getNombre() != null) {
                existente.setNombre(productoActualizado.getNombre());
            }
            if (productoActualizado.getSku() != null) {
                existente.setSku(productoActualizado.getSku());
            }
            if (productoActualizado.getPrecioCosto() != null) {
                existente.setPrecioCosto(productoActualizado.getPrecioCosto());
            }
            if (productoActualizado.getPrecioVenta() != null) {
                existente.setPrecioVenta(productoActualizado.getPrecioVenta());
            }
            if (productoActualizado.getStockTotal() != null) {
                existente.setStockTotal(productoActualizado.getStockTotal());
            }

            Producto actualizado = productoService.actualizar(id, existente);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(404, "Producto no encontrado")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al actualizar: " + e.getMessage())
            );
        }
    }

    // Clase interna para respuestas de error
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