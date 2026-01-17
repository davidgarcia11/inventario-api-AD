package com.example.inventarioapiad;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;

// 1. QUITAMOS la anotación @WireMockTest que causaba el conflicto
public class WireMockApiSimulationTest {

    // 2. Definimos el servidor manualmente
    private WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        // 3. Arrancamos el servidor en el puerto 8081 antes de cada test
        wireMockServer = new WireMockServer(options().port(8081));
        wireMockServer.start();

        // Configuramos el cliente estático de WireMock para que sepa dónde enviar las peticiones
        configureFor("localhost", 8081);
    }

    @AfterEach
    void teardown() {
        // 4. Apagamos el servidor al terminar para limpiar memoria
        wireMockServer.stop();
    }

    @Test
    void testSimulacionApiExterna() {
        stubFor(get(urlEqualTo("/api/impuestos"))
                .willReturn(ok("21")));

        RestTemplate restTemplate = new RestTemplate();
        String respuesta = restTemplate.getForObject("http://localhost:8081/api/impuestos", String.class);

        assertEquals("21", respuesta);
    }

    @Test
    void testSimulacionProductoExterno() {
        stubFor(get(urlEqualTo("/api/precios/TORNILLO-M10"))
                .willReturn(ok("{\"sku\":\"TORNILLO-M10\",\"precio\":1.25}")));

        RestTemplate restTemplate = new RestTemplate();
        String respuesta = restTemplate.getForObject("http://localhost:8081/api/precios/TORNILLO-M10", String.class);

        assertEquals("{\"sku\":\"TORNILLO-M10\",\"precio\":1.25}", respuesta);
    }

    @Test
    void testSimulacionError500() {
        stubFor(get(urlEqualTo("/api/precios/ERROR"))
                .willReturn(serverError()));

        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.getForObject("http://localhost:8081/api/precios/ERROR", String.class);
        } catch (HttpServerErrorException e) {
            assertEquals(500, e.getStatusCode().value());
        }
    }
}