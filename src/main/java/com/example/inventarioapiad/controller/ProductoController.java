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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // CREATE - POST /api/productos
    @PostMapping
    @Operation(summary = "Crear Producto", description = "Crea un nuevo producto en la base de datos. Campos obligatorios: nombre, sku, precioVenta, stockTotal.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Producto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Tornillo M10",
                                      "sku": "TOR-M10",
                                      "precioCosto": 0.50,
                                      "precioVenta": 1.25,
                                      "stockTotal": 100,
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre vacío, precio negativo, etc.)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El SKU es obligatorio y no puede estar vacío"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error interno al guardar el producto"
                                    }
                                    """)))
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
    @Operation(summary = "Obtener Producto por ID", description = "Obtiene los detalles de un producto específico por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Producto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Tornillo M10",
                                      "sku": "TOR-M10",
                                      "precioCosto": 0.50,
                                      "precioVenta": 1.25,
                                      "stockTotal": 100,
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Producto no encontrado con ID: 1"
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
    @Operation(summary = "Listar Productos (con filtros)", description = "Obtiene todos los productos activos. Permite filtrar por nombre, sku y precioVenta exacto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Producto.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": 1,
                                        "nombre": "Tornillo M10",
                                        "sku": "TOR-M10",
                                        "precioVenta": 1.25,
                                        "stockTotal": 100,
                                        "activo": true
                                      },
                                      {
                                        "id": 2,
                                        "nombre": "Tuerca M10",
                                        "sku": "TUE-M10",
                                        "precioVenta": 0.50,
                                        "stockTotal": 500,
                                        "activo": true
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al filtrar productos"
                                    }
                                    """)))
    })
    public ResponseEntity<?> buscarTodos(
            @Parameter(description = "Filtrar por nombre (contiene)") @RequestParam(required = false) String nombre,
            @Parameter(description = "Filtrar por SKU (contiene)") @RequestParam(required = false) String sku,
            @Parameter(description = "Filtrar por precio exacto") @RequestParam(required = false) Float precioVenta) {

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
    @Operation(summary = "Actualizar Completo", description = "Actualiza todos los campos del producto. Si un campo no se envía, se podría perder o poner a null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Producto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Tornillo M10 Inoxidable",
                                      "sku": "TOR-M10-INOX",
                                      "precioVenta": 2.50,
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El precio de venta debe ser mayor a 0"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede actualizar. Producto no encontrado con ID: 1"
                                    }
                                    """))),
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
    @Operation(summary = "Eliminar Producto", description = "Realiza un borrado lógico (Soft Delete): marca el producto como inactivo (activo=false).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado (inactivado) correctamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede eliminar. Producto no encontrado con ID: 1"
                                    }
                                    """))),
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

    // PATCH - Actualización parcial
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar Parcialmente (PATCH)", description = "Actualiza solo los campos proporcionados en el JSON, manteniendo el resto con su valor actual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado parcialmente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Producto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "precioVenta": 1.75,
                                      "stockTotal": 150,
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Producto no encontrado"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID del producto a modificar", example = "1")
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