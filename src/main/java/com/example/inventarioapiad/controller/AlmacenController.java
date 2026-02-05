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
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/almacenes")
public class AlmacenController {

    @Autowired
    private AlmacenService almacenService;

    // CREATE - POST /api/almacenes
    @PostMapping
    @Operation(summary = "Crear Almacén", description = "Crea un nuevo almacén en la base de datos. Campos obligatorios: nombre, ubicacion.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Almacén creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Almacen.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Almacén Central",
                                      "ubicacion": "Madrid",
                                      "capacidadMaxima": 10000,
                                      "stockActual": 500,
                                      "responsable": "Carlos Ruiz",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre vacío, capacidad negativa, etc.)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El nombre del almacén es obligatorio"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error interno al guardar en base de datos"
                                    }
                                    """)))
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
    @Operation(summary = "Obtener Almacén por ID", description = "Obtiene un almacén específico buscando por su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Almacén encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Almacen.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Almacén Central",
                                      "ubicacion": "Madrid",
                                      "capacidadMaxima": 10000,
                                      "stockActual": 500,
                                      "responsable": "Carlos Ruiz",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Almacén no encontrado con ID: 1"
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
    @Operation(summary = "Listar Almacenes (con filtros)", description = "Obtiene todos los almacenes activos. Permite filtrar por nombre, ubicación y capacidad máxima.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de almacenes recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Almacen.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": 1,
                                        "nombre": "Almacén Norte",
                                        "ubicacion": "Bilbao",
                                        "capacidadMaxima": 5000,
                                        "stockActual": 1200,
                                        "activo": true
                                      },
                                      {
                                        "id": 2,
                                        "nombre": "Almacén Sur",
                                        "ubicacion": "Sevilla",
                                        "capacidadMaxima": 8000,
                                        "stockActual": 3000,
                                        "activo": true
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al recuperar la lista de almacenes"
                                    }
                                    """)))
    })
    public ResponseEntity<?> buscarTodos(
            @Parameter(description = "Filtrar por nombre (contiene)") @RequestParam(required = false) String nombre,
            @Parameter(description = "Filtrar por ubicación (contiene)") @RequestParam(required = false) String ubicacion,
            @Parameter(description = "Filtrar por capacidad máxima exacta") @RequestParam(required = false) Integer capacidadMaxima) {

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
    @Operation(summary = "Actualizar Completo", description = "Actualiza todos los campos del almacén. Si un campo no se envía, se podría perder o poner a null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Almacén actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Almacen.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Almacén Central Renovado",
                                      "ubicacion": "Madrid Norte",
                                      "capacidadMaxima": 15000,
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "La capacidad máxima no puede ser negativa"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede actualizar. Almacén no encontrado con ID: 1"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error interno al actualizar el almacén"
                                    }
                                    """)))
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
    @Operation(summary = "Eliminar Almacén", description = "Realiza un borrado lógico (Soft Delete): marca el almacén como inactivo (activo=false).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Almacén eliminado correctamente (Sin contenido)"),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede eliminar. Almacén no encontrado con ID: 1"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error interno al eliminar el almacén"
                                    }
                                    """)))
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
    @Operation(summary = "Actualizar Parcialmente (PATCH)", description = "Actualiza solo los campos proporcionados en el JSON, manteniendo el resto con su valor actual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Almacén actualizado parcialmente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Almacen.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Almacén Central",
                                      "ubicacion": "Nueva Ubicación",
                                      "capacidadMaxima": 10000,
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Almacén no encontrado"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error interno al actualizar parcialmente"
                                    }
                                    """)))
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID del almacén a modificar", example = "1")
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