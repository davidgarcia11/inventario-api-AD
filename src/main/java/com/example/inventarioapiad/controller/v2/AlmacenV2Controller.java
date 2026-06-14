package com.example.inventarioapiad.controller.v2;

import com.example.inventarioapiad.dto.AlmacenCreateRequestV2;
import com.example.inventarioapiad.dto.AlmacenUpdateRequestV2;
import com.example.inventarioapiad.dto.PagedResponse;
import com.example.inventarioapiad.entity.Almacen;
import com.example.inventarioapiad.service.AlmacenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

// ============================================================================
// Endpoints V2 de Almacenes
// ============================================================================
// La V2 introduce el campo "prioritario" (boolean) en la entidad Almacén
// y los 4 endpoints versionados giran TODOS alrededor de él, dándole un
// sentido coherente al versionado:
//
//   GET    /api/v2/almacenes        Paginado, devuelve "prioritario"
//   POST   /api/v2/almacenes        Acepta DTO con "prioritario"
//   PUT    /api/v2/almacenes/{id}   Permite cambiar "prioritario"
//   DELETE /api/v2/almacenes/{id}   409 si el almacén es prioritario
//
// Los V1 (/api/almacenes/...) siguen funcionando exactamente igual y
// devuelven el campo "prioritario" si está presente en la BD (lo
// hereda como cualquier otro campo de la entidad). Pero solo los V2
// añaden lógica de negocio asociada a ese campo.
// ============================================================================
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v2/almacenes")
@Tag(name = "Almacenes V2", description = "Endpoints versionados de Almacén con el nuevo campo 'prioritario'")
public class AlmacenV2Controller {

    @Autowired
    private AlmacenService almacenService;

    // ---------------------------------------------------------------- GET
    @GetMapping
    @Operation(summary = "Listar almacenes paginados (V2)",
               description = "Devuelve una página de almacenes con metadatos (page, size, totalElements...). Incluye el campo 'prioritario' en cada elemento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de almacenes",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Página con un almacén prioritario", value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 1,
                                                  "nombre": "Almacén Central",
                                                  "ubicacion": "Zaragoza",
                                                  "capacidadMaxima": 10000,
                                                  "stockActual": 6500,
                                                  "responsable": "María García",
                                                  "activo": true,
                                                  "prioritario": true,
                                                  "fechaCreacion": "2026-06-07T09:00:00"
                                                },
                                                {
                                                  "id": 2,
                                                  "nombre": "Almacén Norte",
                                                  "ubicacion": "Bilbao",
                                                  "capacidadMaxima": 5000,
                                                  "stockActual": 0,
                                                  "responsable": null,
                                                  "activo": true,
                                                  "prioritario": false,
                                                  "fechaCreacion": "2026-06-07T09:15:00"
                                                }
                                              ],
                                              "page": 0,
                                              "size": 10,
                                              "totalElements": 2,
                                              "totalPages": 1,
                                              "first": true,
                                              "last": true
                                            }
                                            """),
                                    @ExampleObject(name = "Página vacía", value = """
                                            {
                                              "content": [],
                                              "page": 0,
                                              "size": 10,
                                              "totalElements": 0,
                                              "totalPages": 0,
                                              "first": true,
                                              "last": true
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Página negativa", value = """
                                            {
                                              "codigo": 400,
                                              "mensaje": "El número de página no puede ser negativo"
                                            }
                                            """),
                                    @ExampleObject(name = "Tamaño fuera de rango", value = """
                                            {
                                              "codigo": 400,
                                              "mensaje": "El tamaño debe estar entre 1 y 100"
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al listar almacenes: ..."
                                    }
                                    """)))
    })
    public ResponseEntity<?> listarPaginado(
            @Parameter(description = "Número de página (0 indexado)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página (máx 100)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo de ordenación, formato 'campo,asc' o 'campo,desc'")
            @RequestParam(defaultValue = "id,asc") String sort) {

        try {
            if (page < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new ErrorResponseV2(400, "El número de página no puede ser negativo"));
            }
            if (size <= 0 || size > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new ErrorResponseV2(400, "El tamaño debe estar entre 1 y 100"));
            }

            String[] partes = sort.split(",");
            String campo = partes[0];
            Sort.Direction direccion = (partes.length > 1 && partes[1].equalsIgnoreCase("desc"))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direccion, campo));
            Page<Almacen> resultados = almacenService.buscarPaginado(pageable);

            return ResponseEntity.ok(PagedResponse.desde(resultados));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponseV2(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponseV2(500, "Error al listar almacenes: " + e.getMessage()));
        }
    }

    // --------------------------------------------------------------- POST
    @PostMapping
    @Operation(summary = "Crear almacén (V2)",
               description = "Crea un almacén con el nuevo campo 'prioritario'. Devuelve 201 + cabecera Location.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Almacén creado",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Almacén prioritario", value = """
                                            {
                                              "id": 1,
                                              "nombre": "Almacén Central",
                                              "ubicacion": "Zaragoza",
                                              "capacidadMaxima": 10000,
                                              "stockActual": 0,
                                              "responsable": "María García",
                                              "activo": true,
                                              "prioritario": true,
                                              "fechaCreacion": "2026-06-14T10:30:00"
                                            }
                                            """),
                                    @ExampleObject(name = "Almacén no prioritario", value = """
                                            {
                                              "id": 2,
                                              "nombre": "Almacén Auxiliar",
                                              "ubicacion": "Madrid",
                                              "capacidadMaxima": 2000,
                                              "stockActual": 0,
                                              "responsable": null,
                                              "activo": true,
                                              "prioritario": false,
                                              "fechaCreacion": "2026-06-14T10:35:00"
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Nombre vacío", value = """
                                            {
                                              "codigo": 400,
                                              "mensaje": "El nombre del almacén es obligatorio"
                                            }
                                            """),
                                    @ExampleObject(name = "Capacidad no positiva", value = """
                                            {
                                              "codigo": 400,
                                              "mensaje": "La capacidad máxima debe ser mayor a 0"
                                            }
                                            """)
                            })),
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

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(creado.getId())
                    .toUri();

            return ResponseEntity.created(location).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponseV2(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponseV2(500, "Error al crear el almacén: " + e.getMessage()));
        }
    }

    // ---------------------------------------------------------------- PUT
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar almacén (V2)",
               description = "Actualiza el almacén. Permite cambiar el campo 'prioritario'. La capacidad máxima ya no se modifica desde aquí.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Almacén actualizado",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Marcando como prioritario", value = """
                                            {
                                              "id": 1,
                                              "nombre": "Almacén Central",
                                              "ubicacion": "Zaragoza",
                                              "capacidadMaxima": 10000,
                                              "stockActual": 6500,
                                              "responsable": "María García",
                                              "activo": true,
                                              "prioritario": true,
                                              "fechaCreacion": "2026-06-07T09:00:00"
                                            }
                                            """),
                                    @ExampleObject(name = "Solo cambiando stock", value = """
                                            {
                                              "id": 1,
                                              "nombre": "Almacén Central",
                                              "ubicacion": "Zaragoza",
                                              "capacidadMaxima": 10000,
                                              "stockActual": 9000,
                                              "responsable": "María García",
                                              "activo": true,
                                              "prioritario": false,
                                              "fechaCreacion": "2026-06-07T09:00:00"
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Nombre vacío", value = """
                                            {
                                              "codigo": 400,
                                              "mensaje": "El nombre del almacén es obligatorio"
                                            }
                                            """),
                                    @ExampleObject(name = "Stock negativo", value = """
                                            {
                                              "codigo": 400,
                                              "mensaje": "El stock actual no puede ser negativo"
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Almacén no encontrado con ID: 999"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al actualizar el almacén: ..."
                                    }
                                    """)))
    })
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody AlmacenUpdateRequestV2 request) {
        try {
            // Buscamos el existente para que tire 404 si no está
            Almacen existente = almacenService.buscarPorId(id);

            // Aplicamos solo los campos permitidos por el DTO de V2
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

    // ------------------------------------------------------------- DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar almacén (V2)",
               description = "Borrado lógico (soft delete) condicional: si el almacén es prioritario, se rechaza con 409.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Almacén eliminado (sin cuerpo en la respuesta)"),
            @ApiResponse(responseCode = "400", description = "ID inválido",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El ID debe ser válido"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Almacén no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Almacén no encontrado con ID: 999"
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "Conflict: el almacén es prioritario",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Almacén prioritario", value = """
                                    {
                                      "codigo": 409,
                                      "mensaje": "No se puede eliminar un almacén prioritario. Marca prioritario=false antes de borrar."
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al eliminar el almacén: ..."
                                    }
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
            // Almacén prioritario -> 409 Conflict
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
