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
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
    @Operation(summary = "Crear Proveedor", description = "Crea un nuevo proveedor en la base de datos. Campos obligatorios: nombre, email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Proveedor creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Proveedor.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Aceros García",
                                      "email": "contacto@acerosgarcia.com",
                                      "telefono": "912345678",
                                      "pais": "España",
                                      "diasEntrega": 3,
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre vacío, email incorrecto, etc.)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El nombre del proveedor es obligatorio"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error interno al guardar el proveedor"
                                    }
                                    """)))
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
    @Operation(summary = "Obtener Proveedor por ID", description = "Obtiene los detalles de un proveedor específico buscando por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Proveedor.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Aceros García",
                                      "email": "contacto@acerosgarcia.com",
                                      "telefono": "912345678",
                                      "pais": "España",
                                      "diasEntrega": 3,
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Proveedor no encontrado con ID: 1"
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
    @Operation(summary = "Listar Proveedores (con filtros)", description = "Obtiene todos los proveedores activos. Permite filtrar por nombre, email y días de entrega exactos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de proveedores recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Proveedor.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": 1,
                                        "nombre": "Aceros García",
                                        "email": "contacto@aceros.com",
                                        "diasEntrega": 3,
                                        "activo": true
                                      },
                                      {
                                        "id": 2,
                                        "nombre": "Transportes Rápidos",
                                        "email": "info@rapidos.com",
                                        "diasEntrega": 1,
                                        "activo": true
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al filtrar proveedores"
                                    }
                                    """)))
    })
    public ResponseEntity<?> buscarTodos(
            @Parameter(description = "Filtrar por nombre (contiene)") @RequestParam(required = false) String nombre,
            @Parameter(description = "Filtrar por email (contiene)") @RequestParam(required = false) String email,
            @Parameter(description = "Filtrar por días de entrega exactos") @RequestParam(required = false) Integer diasEntrega) {

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
    @Operation(summary = "Actualizar Completo", description = "Actualiza todos los campos del proveedor. Si un campo no se envía, se podría perder o poner a null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Proveedor.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Aceros García S.L.",
                                      "email": "nuevo.contacto@acerosgarcia.com",
                                      "pais": "Portugal",
                                      "diasEntrega": 5,
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "Los días de entrega no pueden ser negativos"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede actualizar. Proveedor no encontrado con ID: 1"
                                    }
                                    """))),
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
    @Operation(summary = "Eliminar Proveedor", description = "Realiza un borrado lógico (Soft Delete): marca el proveedor como inactivo (activo=false).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Proveedor eliminado (inactivado) correctamente"),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede eliminar. Proveedor no encontrado con ID: 1"
                                    }
                                    """))),
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
    @Operation(summary = "Actualizar Parcialmente (PATCH)", description = "Actualiza solo los campos proporcionados en el JSON, manteniendo el resto con su valor actual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor actualizado parcialmente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Proveedor.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "email": "email.urgente@aceros.com",
                                      "diasEntrega": 2,
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Proveedor no encontrado"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID del proveedor a modificar", example = "1")
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