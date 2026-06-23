package com.example.inventarioapiad.controller.v2;

import com.example.inventarioapiad.dto.AlmacenCreateRequestV2;
import com.example.inventarioapiad.dto.AlmacenUpdateRequestV2;
import com.example.inventarioapiad.entity.Almacen;
import com.example.inventarioapiad.service.AlmacenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ============================================================================
// Endpoints V2 de Almacenes
// ============================================================================
// La V2 tiene exactamente los mismos 6 endpoints que la V1 (POST, GET
// por id, GET lista con filtros, PUT, PATCH, DELETE) y se diferencia
// en dos cosas concretas:
//
//   1) Acepta y devuelve el campo nuevo "prioritario" (boolean) en todas
//      las operaciones de escritura y lectura.
//   2) DELETE rechaza con 409 Conflict cuando el almacén está marcado
//      como prioritario. La V1 borra sin mirar ese campo.
//
// Además se añade en el GET lista un filtro opcional ?prioritario=true|false
// para poder pedir solo los almacenes prioritarios o solo los no prioritarios.
//
// Cada endpoint gestiona explícitamente 400/404/409/500 con un cuerpo
// uniforme {codigo, mensaje}.
// ============================================================================
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v2/almacenes")
@Tag(name = "Almacenes V2", description = "Endpoints V2 de Almacén con el nuevo campo 'prioritario'")
public class AlmacenV2Controller {

    @Autowired
    private AlmacenService almacenService;

    // ---------------------------------------------------------------- POST
    @PostMapping
    @Operation(summary = "Crear Almacén (V2)",
               description = "Crea un almacén aceptando el nuevo campo 'prioritario'.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Almacén creado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Almacen.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Almacén Central",
                                      "ubicacion": "Zaragoza",
                                      "capacidadMaxima": 10000,
                                      "stockActual": 0,
                                      "responsable": "María García",
                                      "activo": true,
                                      "prioritario": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El nombre del almacén es obligatorio"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al crear el almacén: ..."
                                    }
                                    """)))
    })
    public ResponseEntity<?> crear(@Valid @RequestBody AlmacenCreateRequestV2 request) {
        try {
            Almacen creado = almacenService.crear(request.toEntity());
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponseV2(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponseV2(500, "Error al crear el almacén: " + e.getMessage()));
        }
    }

    // ------------------------------------------------------- GET por id
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Almacén por ID (V2)",
               description = "Devuelve un almacén concreto, incluyendo el campo 'prioritario'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Almacén encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Almacen.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 400, "mensaje": "El ID debe ser válido"}
                                    """))),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 404, "mensaje": "Almacén no encontrado con ID: 999"}
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 500, "mensaje": "Error al buscar el almacén: ..."}
                                    """)))
    })
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Almacen almacen = almacenService.buscarPorId(id);
            return ResponseEntity.ok(almacen);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponseV2(400, e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponseV2(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponseV2(500, "Error al buscar el almacén: " + e.getMessage()));
        }
    }

    // ----------------------------------------------------------- GET lista
    @GetMapping
    @Operation(summary = "Listar Almacenes (V2)",
               description = "Lista los almacenes. Acepta los mismos filtros que la V1 y añade ?prioritario=true|false para filtrar por ese campo nuevo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de almacenes",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Almacen.class))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 500, "mensaje": "Error al filtrar almacenes: ..."}
                                    """)))
    })
    public ResponseEntity<?> buscarTodos(
            @Parameter(description = "Filtrar por nombre (contiene)")
            @RequestParam(required = false) String nombre,
            @Parameter(description = "Filtrar por ubicación (contiene)")
            @RequestParam(required = false) String ubicacion,
            @Parameter(description = "Filtrar por capacidad máxima exacta")
            @RequestParam(required = false) Integer capacidadMaxima,
            @Parameter(description = "Filtrar por el campo prioritario (true|false). Nuevo en V2.")
            @RequestParam(required = false) Boolean prioritario) {
        try {
            List<Almacen> resultados = almacenService.buscarConFiltrosV2(
                    nombre, ubicacion, capacidadMaxima, prioritario);
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponseV2(500, "Error al filtrar almacenes: " + e.getMessage()));
        }
    }

    // ---------------------------------------------------------------- PUT
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar Almacén (V2)",
               description = "Actualiza el almacén entero, permitiendo cambiar el campo 'prioritario'. La capacidad máxima no se modifica desde aquí.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Almacén actualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Almacen.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 400, "mensaje": "El nombre del almacén es obligatorio"}
                                    """))),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 404, "mensaje": "Almacén no encontrado con ID: 999"}
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 500, "mensaje": "Error al actualizar el almacén: ..."}
                                    """)))
    })
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody AlmacenUpdateRequestV2 request) {
        try {
            Almacen existente = almacenService.buscarPorId(id);

            existente.setNombre(request.getNombre());
            existente.setUbicacion(request.getUbicacion());
            if (request.getStockActual() != null) {
                existente.setStockActual(request.getStockActual());
            }
            if (request.getResponsable() != null) {
                existente.setResponsable(request.getResponsable());
            }
            if (request.getPrioritario() != null) {
                existente.setPrioritario(request.getPrioritario());
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

    // --------------------------------------------------------------- PATCH
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente (V2)",
               description = "Actualiza solo los campos enviados en el JSON, manteniendo el resto. Permite cambiar 'prioritario' sin tocar el resto del almacén.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Almacén actualizado parcialmente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Almacen.class))),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 404, "mensaje": "Almacén no encontrado con ID: 999"}
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 500, "mensaje": "Error al actualizar parcialmente: ..."}
                                    """)))
    })
    public ResponseEntity<?> actualizarParcial(@PathVariable Long id,
                                               @RequestBody Almacen cambios) {
        try {
            Almacen existente = almacenService.buscarPorId(id);

            if (cambios.getNombre() != null) {
                existente.setNombre(cambios.getNombre());
            }
            if (cambios.getUbicacion() != null) {
                existente.setUbicacion(cambios.getUbicacion());
            }
            if (cambios.getStockActual() != null) {
                existente.setStockActual(cambios.getStockActual());
            }
            if (cambios.getResponsable() != null) {
                existente.setResponsable(cambios.getResponsable());
            }
            // El cambio "estrella" del PATCH V2: poder marcar / desmarcar el flag.
            if (cambios.getPrioritario() != null) {
                existente.setPrioritario(cambios.getPrioritario());
            }

            Almacen actualizado = almacenService.actualizar(id, existente);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponseV2(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponseV2(500, "Error al actualizar parcialmente: " + e.getMessage()));
        }
    }

    // ------------------------------------------------------------- DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Almacén (V2)",
               description = "Soft delete condicional. Si el almacén está marcado como prioritario, devuelve 409 Conflict en vez de borrarlo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Almacén eliminado (sin cuerpo)"),
            @ApiResponse(responseCode = "400", description = "ID inválido",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 400, "mensaje": "El ID debe ser válido"}
                                    """))),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 404, "mensaje": "Almacén no encontrado con ID: 999"}
                                    """))),
            @ApiResponse(responseCode = "409", description = "Conflict: el almacén es prioritario",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 409, "mensaje": "No se puede eliminar un almacén prioritario. Marca prioritario=false antes de borrar."}
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {"codigo": 500, "mensaje": "Error al eliminar el almacén: ..."}
                                    """)))
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            almacenService.eliminarSiNoPrioritario(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponseV2(400, e.getMessage()));
        } catch (IllegalStateException e) {
            // Almacén prioritario -> 409 Conflict (lo característico de la V2)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ErrorResponseV2(409, e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponseV2(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponseV2(500, "Error al eliminar el almacén: " + e.getMessage()));
        }
    }
}
