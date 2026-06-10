package com.example.inventarioapiad.controller.v2;

import com.example.inventarioapiad.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ----------------------------------------------------------------------
// Endpoint V2 de Proveedores
// ----------------------------------------------------------------------
// Cambio respecto a la V1:
//   La V1 (DELETE /api/proveedores/{id}) hace SIEMPRE soft delete:
//   marca activo=false pero el registro sigue en la BD. No hay forma
//   desde la API de borrarlo de verdad.
//
//   La V2 (DELETE /api/v2/proveedores/{id}) sigue por defecto el soft
//   delete, pero acepta un query param ?hard=true para borrar el
//   registro de verdad (delete físico). Útil para limpieza administrativa
//   o cumplimiento de privacidad (GDPR).
// ----------------------------------------------------------------------
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v2/proveedores")
public class ProveedorV2Controller {

    @Autowired
    private ProveedorService proveedorService;

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proveedor (V2)",
               description = "Soft delete por defecto. Con ?hard=true borra el registro de la BD.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Proveedor eliminado. Sin cuerpo en la respuesta."),
            @ApiResponse(responseCode = "400", description = "ID inválido",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 400,
                                      "mensaje": "El ID debe ser válido"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "Soft delete sobre id inexistente", value = """
                                            {
                                              "codigo": 404,
                                              "mensaje": "Proveedor no encontrado con ID: 999"
                                            }
                                            """),
                                    @ExampleObject(name = "Hard delete sobre id inexistente", value = """
                                            {
                                              "codigo": 404,
                                              "mensaje": "Proveedor no encontrado con ID: 999"
                                            }
                                            """)
                            })),
            @ApiResponse(responseCode = "500", description = "Error interno",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "codigo": 500,
                                      "mensaje": "Error al eliminar el proveedor: ..."
                                    }
                                    """)))
    })
    public ResponseEntity<?> eliminar(
            @PathVariable Long id,
            @Parameter(description = "Si true, borra físicamente el registro de la BD.")
            @RequestParam(defaultValue = "false") boolean hard) {
        try {
            if (hard) {
                proveedorService.eliminarFisico(id);
            } else {
                proveedorService.eliminar(id);
            }
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponseV2(400, e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ErrorResponseV2(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponseV2(500, "Error al eliminar el proveedor: " + e.getMessage()));
        }
    }
}
