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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    // CREATE - POST /api/compras
    @PostMapping
    @Operation(summary = "Registrar Compra", description = "Crea un nuevo registro de compra relacionando un proveedor, producto y almacén. Campos obligatorios: proveedor, producto, almacen, cantidad, precioUnitario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Compra registrada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Compra.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "proveedor": { "id": 1, "nombre": "Aceros García" },
                                      "producto": { "id": 5, "nombre": "Tornillo M10" },
                                      "almacen": { "id": 2, "nombre": "Almacén Central" },
                                      "cantidad": 100,
                                      "precioUnitario": 1.50,
                                      "fechaCompra": "2025-02-20T10:00:00",
                                      "numeroFactura": "FAC-2025-001",
                                      "estado": "RECIBIDA",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (cantidad negativa, IDs no existen, etc.)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "La cantidad de la compra debe ser mayor a 0"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al procesar la compra en base de datos"
                                    }
                                    """)))
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
    @Operation(summary = "Obtener Compra por ID", description = "Obtiene los detalles completos de una compra específica por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compra encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Compra.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "proveedor": { "id": 1, "nombre": "Aceros García" },
                                      "producto": { "id": 5, "nombre": "Tornillo M10" },
                                      "almacen": { "id": 2, "nombre": "Almacén Central" },
                                      "cantidad": 100,
                                      "precioUnitario": 1.50,
                                      "fechaCompra": "2025-02-20T10:00:00",
                                      "numeroFactura": "FAC-2025-001",
                                      "estado": "RECIBIDA",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Compra no encontrada",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Compra no encontrada con ID: 1"
                                    }
                                    """))),
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
    @Operation(summary = "Listar Compras (con filtros)", description = "Obtiene todas las compras registradas. Permite filtrar por estado, cantidad exacta y número de factura.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de compras recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Compra.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": 1,
                                        "proveedor": { "id": 1, "nombre": "Aceros García" },
                                        "cantidad": 100,
                                        "numeroFactura": "FAC-001",
                                        "estado": "RECIBIDA",
                                        "activo": true
                                      },
                                      {
                                        "id": 2,
                                        "proveedor": { "id": 2, "nombre": "Ferretería Industrial" },
                                        "cantidad": 50,
                                        "numeroFactura": "FAC-002",
                                        "estado": "PENDIENTE",
                                        "activo": true
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al filtrar las compras"
                                    }
                                    """)))
    })
    public ResponseEntity<?> buscarTodos(
            @Parameter(description = "Filtrar por estado exacto (ej: RECIBIDA, PENDIENTE)") @RequestParam(required = false) String estado,
            @Parameter(description = "Filtrar por cantidad exacta") @RequestParam(required = false) Integer cantidad,
            @Parameter(description = "Filtrar por número de factura (contiene)") @RequestParam(required = false) String numeroFactura) {

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
    @Operation(summary = "Actualizar Completo", description = "Actualiza todos los campos de la compra (reemplazo completo).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compra actualizada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Compra.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "proveedor": { "id": 1 },
                                      "producto": { "id": 5 },
                                      "almacen": { "id": 2 },
                                      "cantidad": 150,
                                      "precioUnitario": 1.45,
                                      "numeroFactura": "FAC-001-MOD",
                                      "estado": "PAGADA",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El precio unitario no puede ser negativo"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Compra no encontrada",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede actualizar. Compra no encontrada con ID: 1"
                                    }
                                    """))),
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
    @Operation(summary = "Eliminar Compra", description = "Realiza un borrado lógico (Soft Delete): Marca la compra como inactiva (activo=false) o cancelada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Compra eliminada (inactivada) correctamente"),
            @ApiResponse(responseCode = "404", description = "Compra no encontrada",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede eliminar. Compra no encontrada con ID: 1"
                                    }
                                    """))),
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
    @Operation(summary = "Actualizar Parcialmente (PATCH)", description = "Actualiza solo los campos proporcionados (ej: cambiar estado o corregir cantidad).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compra actualizada parcialmente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Compra.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "numeroFactura": "FAC-001",
                                      "estado": "PAGADA",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Compra no encontrada",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Compra no encontrada"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID de la compra a modificar", example = "1")
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