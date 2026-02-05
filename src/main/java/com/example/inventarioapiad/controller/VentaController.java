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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    // CREATE - POST /api/ventas
    @PostMapping
    @Operation(summary = "Registrar Venta", description = "Crea un nuevo registro de venta. Campos obligatorios: cliente, producto, almacen, cantidad, precioUnitario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venta registrada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Venta.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "cliente": { "id": 1, "nombre": "Juan Pérez" },
                                      "producto": { "id": 5, "nombre": "Tornillo M10" },
                                      "almacen": { "id": 2, "nombre": "Almacén Central" },
                                      "cantidad": 50,
                                      "precioUnitario": 2.50,
                                      "fechaVenta": "2025-02-21T16:30:00",
                                      "numeroPedido": "PED-2025-001",
                                      "estado": "ENTREGADA",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (cantidad negativa, cliente/producto no existe, etc.)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El stock disponible no es suficiente para realizar la venta"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error interno al procesar la venta"
                                    }
                                    """)))
    })
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
    @Operation(summary = "Obtener Venta por ID", description = "Obtiene los detalles completos de una venta específica por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Venta.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "cliente": { "id": 1, "nombre": "Juan Pérez" },
                                      "producto": { "id": 5, "nombre": "Tornillo M10" },
                                      "almacen": { "id": 2, "nombre": "Almacén Central" },
                                      "cantidad": 50,
                                      "precioUnitario": 2.50,
                                      "fechaVenta": "2025-02-21T16:30:00",
                                      "numeroPedido": "PED-2025-001",
                                      "estado": "ENTREGADA",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Venta no encontrada con ID: 1"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error de conexión con la base de datos"
                                    }
                                    """)))
    })
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
    @Operation(summary = "Listar Ventas (con filtros)", description = "Obtiene todas las ventas registradas. Permite filtrar por estado exacto, cantidad exacta y número de pedido.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de ventas recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Venta.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": 1,
                                        "cliente": { "id": 1, "nombre": "Juan Pérez" },
                                        "cantidad": 50,
                                        "numeroPedido": "PED-001",
                                        "estado": "ENTREGADA",
                                        "activo": true
                                      },
                                      {
                                        "id": 2,
                                        "cliente": { "id": 2, "nombre": "Empresa S.A." },
                                        "cantidad": 100,
                                        "numeroPedido": "PED-002",
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
                                      "mensaje": "Error al filtrar las ventas"
                                    }
                                    """)))
    })
    public ResponseEntity<?> buscarTodos(
            @Parameter(description = "Filtrar por estado exacto (ej: ENTREGADA, PENDIENTE)") @RequestParam(required = false) String estado,
            @Parameter(description = "Filtrar por cantidad exacta") @RequestParam(required = false) Integer cantidad,
            @Parameter(description = "Filtrar por número de pedido (contiene)") @RequestParam(required = false) String numeroPedido) {

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
    @Operation(summary = "Actualizar Completo", description = "Actualiza todos los campos de la venta (reemplazo completo).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta actualizada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Venta.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "cliente": { "id": 1 },
                                      "producto": { "id": 5 },
                                      "almacen": { "id": 2 },
                                      "cantidad": 60,
                                      "precioUnitario": 2.40,
                                      "numeroPedido": "PED-001-MOD",
                                      "estado": "FACTURADA",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "La cantidad no puede ser negativa"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede actualizar. Venta no encontrada con ID: 1"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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
    @Operation(summary = "Eliminar Venta", description = "Realiza un borrado lógico (Soft Delete): Marca la venta como inactiva (activo=false) o cancelada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Venta eliminada (inactivada) correctamente"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede eliminar. Venta no encontrada con ID: 1"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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

    // PATCH - Actualización parcial
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar Parcialmente (PATCH)", description = "Actualiza solo los campos proporcionados (ej: cambiar estado o corregir cantidad).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta actualizada parcialmente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Venta.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "numeroPedido": "PED-001",
                                      "estado": "FACTURADA",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Venta no encontrada"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID de la venta a modificar", example = "1")
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