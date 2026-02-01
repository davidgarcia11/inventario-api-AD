package com.example.inventarioapiad;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WireMockApiSimulationTest {

    // Apuntamos al servidor que has arrancado tú manualmente en la terminal
    private final String BASE_URL = "http://localhost:8081";

    @Test
    void testSimulacionApiExterna_Impuestos() {
        RestTemplate restTemplate = new RestTemplate();
        // Llamamos al servidor externo
        String respuesta = restTemplate.getForObject(BASE_URL + "/api/impuestos", String.class);

        // Verificamos que devuelve lo que pusiste en impuestos.json
        assertEquals("21", respuesta);
    }

    @Test
    void testSimulacionProductoExterno_Precios() {
        RestTemplate restTemplate = new RestTemplate();
        String respuesta = restTemplate.getForObject(BASE_URL + "/api/precios/TORNILLO-M10", String.class);

        // Verificamos que devuelve lo que pusiste en precios.json
        assertEquals("{\"sku\":\"TORNILLO-M10\",\"precio\":1.25}", respuesta);
    }

    @Test
    void testSimulacionApiExterna_Error_500() {
        RestTemplate restTemplate = new RestTemplate();

        // Verificamos que salta el error definido en error-500.json
        HttpServerErrorException excepcion = assertThrows(HttpServerErrorException.class, () -> {
            restTemplate.getForObject(BASE_URL + "/api/precios/ERROR-SERVER", String.class);
        });

        assertEquals(500, excepcion.getStatusCode().value());
    }

    @Test
    void testSimulacionApiExterna_Error_404() {
        RestTemplate restTemplate = new RestTemplate();

        // Verificamos que salta el error definido en error-404.json
        HttpClientErrorException excepcion = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.getForObject(BASE_URL + "/api/precios/NO-EXISTE", String.class);
        });

        assertEquals(404, excepcion.getStatusCode().value());
    }
}