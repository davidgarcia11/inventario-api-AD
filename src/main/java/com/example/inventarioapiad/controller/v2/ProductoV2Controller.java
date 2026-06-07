package com.example.inventarioapiad.controller.v2;

import com.example.inventarioapiad.dto.PagedResponse;
import com.example.inventarioapiad.entity.Producto;
import com.example.inventarioapiad.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ----------------------------------------------------------------------
// Endpoint V2 de Productos
// ----------------------------------------------------------------------
// Cambio respecto a la V1:
//   La V1 (GET /api/productos) devuelve TODA la lista de productos en
//   un único array. Eso no escala: cuando hay miles, el cliente recibe
//   una respuesta enorme y la BD sufre.
//
//   La V2 (GET /api/v2/productos) devuelve una respuesta PAGINADA con
//   metadatos (totalElements, totalPages, etc.) y acepta los parámetros
//   estándar de Spring Data: page, size y sort.
// ----------------------------------------------------------------------
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v2/productos")
public class ProductoV2Controller {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar productos paginados (V2)",
               description = "Devuelve un PagedResponse con los productos. Acepta page, size y sort.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de productos"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
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

            // Parseo simple del parámetro sort: "campo,asc" o "campo,desc"
            String[] partes = sort.split(",");
            String campo = partes[0];
            Sort.Direction direccion = (partes.length > 1 && partes[1].equalsIgnoreCase("desc"))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;

            Pageable pageable = PageRequest.of(page, size, Sort.by(direccion, campo));
            Page<Producto> resultados = productoService.buscarPaginado(pageable);

            return ResponseEntity.ok(PagedResponse.desde(resultados));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new ErrorResponseV2(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponseV2(500, "Error al listar productos: " + e.getMessage()));
        }
    }
}
