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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // CREATE - POST /api/clientes
    @PostMapping
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
    public ResponseEntity<?> buscarTodos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String ciudad) {

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

    @PatchMapping("/{id}")
    @Operation(summary = "Actualizar parcialmente un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado parcialmente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> actualizarParcial(
            @Parameter(description = "ID del cliente", example = "1")
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