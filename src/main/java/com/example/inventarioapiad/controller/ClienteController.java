package com.example.inventarioapiad.controller;

import com.example.inventarioapiad.entity.Cliente;
import com.example.inventarioapiad.service.ClienteService;
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
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // CREATE - POST /api/clientes
    @PostMapping
    @Operation(summary = "Crear Cliente", description = "Crea un nuevo cliente en la base de datos. Campos obligatorios: nombre, email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Cliente.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Juan Pérez",
                                      "email": "juan.perez@email.com",
                                      "telefono": "600123456",
                                      "direccion": "Calle Mayor 10",
                                      "ciudad": "Madrid",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (nombre vacío, email inválido, etc.)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El email del cliente es obligatorio"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error interno al guardar el cliente"
                                    }
                                    """)))
    })
    public ResponseEntity<?> crear(@RequestBody Cliente cliente) {
        try {
            Cliente creado = clienteService.crear(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponse(400, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al crear el cliente: " + e.getMessage())
            );
        }
    }

    // READ - GET /api/clientes/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener Cliente por ID", description = "Obtiene un cliente específico buscando por su identificador único.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Cliente.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Juan Pérez",
                                      "email": "juan.perez@email.com",
                                      "telefono": "600123456",
                                      "direccion": "Calle Mayor 10",
                                      "ciudad": "Madrid",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Cliente no encontrado con ID: 1"
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
            Cliente cliente = clienteService.buscarPorId(id);
            return ResponseEntity.ok(cliente);
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
                    new ErrorResponse(500, "Error al buscar el cliente: " + e.getMessage())
            );
        }
    }

    // READ ALL - GET /api/clientes
    @GetMapping
    @Operation(summary = "Listar Clientes (con filtros)", description = "Obtiene todos los clientes activos. Permite filtrar por nombre, email y ciudad.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes recuperada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Cliente.class),
                            examples = @ExampleObject(value = """
                                    [
                                      {
                                        "id": 1,
                                        "nombre": "Juan Pérez",
                                        "email": "juan@email.com",
                                        "ciudad": "Madrid",
                                        "activo": true
                                      },
                                      {
                                        "id": 2,
                                        "nombre": "Ana García",
                                        "email": "ana@email.com",
                                        "ciudad": "Barcelona",
                                        "activo": true
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al filtrar clientes"
                                    }
                                    """)))
    })
    public ResponseEntity<?> buscarTodos(
            @Parameter(description = "Filtrar por nombre (contiene)") @RequestParam(required = false) String nombre,
            @Parameter(description = "Filtrar por email (contiene)") @RequestParam(required = false) String email,
            @Parameter(description = "Filtrar por ciudad (contiene)") @RequestParam(required = false) String ciudad) {

        try {
            List<Cliente> clientes = clienteService.buscarConFiltros(nombre, email, ciudad);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse(500, "Error al filtrar clientes: " + e.getMessage())
            );
        }
    }

    // UPDATE - PUT /api/clientes/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar Completo", description = "Actualiza todos los campos del cliente. Si un campo no se envía, se podría perder o poner a null.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Cliente.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Juan Pérez Actualizado",
                                      "email": "juan.nuevo@email.com",
                                      "ciudad": "Valencia",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El nombre no puede estar vacío"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede actualizar. Cliente no encontrado con ID: 1"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Cliente clienteActualizado) {
        try {
            Cliente actualizado = clienteService.actualizar(id, clienteActualizado);
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
                    new ErrorResponse(500, "Error al actualizar el cliente: " + e.getMessage())
            );
        }
    }

    // DELETE - DELETE /api/clientes/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Cliente", description = "Realiza un borrado lógico (Soft Delete): marca el cliente como inactivo (activo=false).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente eliminado correctamente (Sin contenido)"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "No se puede eliminar. Cliente no encontrado con ID: 1"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            clienteService.eliminar(id);
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
                    new ErrorResponse(500, "Error al eliminar el cliente: " + e.getMessage())
            );
        }
    }

    // PATCH - Actualización parcial
    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar Parcialmente (PATCH)", description = "Actualiza solo los campos proporcionados en el JSON, manteniendo el resto con su valor actual.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado parcialmente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Cliente.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "nombre": "Juan Pérez",
                                      "email": "juan.nuevo@email.com",
                                      "telefono": "600123456",
                                      "ciudad": "Madrid",
                                      "activo": true
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 404,
                                      "mensaje": "Cliente no encontrado"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID del cliente a modificar", example = "1")
            @PathVariable Long id,
            @RequestBody Cliente clienteActualizado) {
        try {
            Cliente existente = clienteService.buscarPorId(id);

            if (clienteActualizado.getNombre() != null) {
                existente.setNombre(clienteActualizado.getNombre());
            }
            if (clienteActualizado.getEmail() != null) {
                existente.setEmail(clienteActualizado.getEmail());
            }
            if (clienteActualizado.getTelefono() != null) {
                existente.setTelefono(clienteActualizado.getTelefono());
            }
            if (clienteActualizado.getDireccion() != null) {
                existente.setDireccion(clienteActualizado.getDireccion());
            }
            if (clienteActualizado.getCiudad() != null) {
                existente.setCiudad(clienteActualizado.getCiudad());
            }

            Cliente actualizado = clienteService.actualizar(id, existente);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponse(404, "Cliente no encontrado")
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