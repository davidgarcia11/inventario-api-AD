package com.example.inventarioapiad.controller.v2;

import com.example.inventarioapiad.dto.ClienteCreateRequest;
import com.example.inventarioapiad.entity.Cliente;
import com.example.inventarioapiad.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

// ----------------------------------------------------------------------
// Endpoint V2 de Clientes
// ----------------------------------------------------------------------
// Cambio respecto a la V1:
//   La V1 (POST /api/clientes) recibe directamente la entidad Cliente y
//   solo valida lo mínimo. Cualquier campo puede llegar (incluso 'id' o
//   'activo'), lo cual no es ideal.
//
//   La V2 (POST /api/v2/clientes) recibe un DTO específico
//   (ClienteCreateRequest) que:
//     - Obliga a enviar email y valida que tenga formato correcto
//       (@Email de jakarta.validation).
//     - No acepta id, activo ni fechaCreacion: los pone el servidor.
//   Además, devuelve un 201 Created con el header Location apuntando al
//   recurso recién creado (buena práctica REST).
// ----------------------------------------------------------------------
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v2/clientes")
public class ClienteV2Controller {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Crear cliente (V2)",
               description = "Crea un cliente nuevo a partir de un DTO con email obligatorio y validado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<?> crear(@Valid @RequestBody ClienteCreateRequest request) {
        try {
            Cliente creado = clienteService.crear(request.toEntity());

            // Devolvemos la URL del recurso recién creado en la cabecera
            // Location, como recomienda la guía REST.
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
                    new ErrorResponseV2(500, "Error al crear el cliente: " + e.getMessage()));
        }
    }
}
