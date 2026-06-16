package com.example.inventarioapiad.config;

import com.example.inventarioapiad.entity.Almacen;
import com.example.inventarioapiad.repository.AlmacenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

// Configuración SOLO activa con el perfil "dev".
// En perfil prod o docker este bean NO se crea, así que la BD arranca vacía.
//
// Sirve para que dev y prod tengan un comportamiento visiblemente distinto
// (no solo cambia la conexión a BD): en dev el alumno/profesor ve datos
// de muestra nada más arrancar, en prod no hay nada.
@Configuration
@Profile("dev")
public class DataSeederConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSeederConfig.class);

    @Bean
    public CommandLineRunner cargarAlmacenesDeMuestra(AlmacenRepository almacenRepository) {
        return args -> {
            log.info("==============================================================");
            log.info(" Perfil DEV activo: comprobando si hay que cargar datos");
            log.info("==============================================================");

            if (almacenRepository.count() > 0) {
                log.info(" Ya existen almacenes en la BD, no se insertan datos de muestra");
                return;
            }

            // 1) Almacén prioritario: no se puede borrar desde /api/v2/almacenes/{id}
            //    sin antes pasar prioritario=false (devuelve 409 Conflict).
            Almacen central = new Almacen();
            central.setNombre("Almacén Central");
            central.setUbicacion("Zaragoza");
            central.setCapacidadMaxima(10000);
            central.setStockActual(6500);
            central.setResponsable("María García");
            central.setPrioritario(true);

            // 2) Almacén normal con stock.
            Almacen norte = new Almacen();
            norte.setNombre("Almacén Norte");
            norte.setUbicacion("Bilbao");
            norte.setCapacidadMaxima(5000);
            norte.setStockActual(1200);
            norte.setResponsable("Carlos Ruiz");
            norte.setPrioritario(false);

            // 3) Almacén normal vacío, para probar el camino feliz del DELETE V2.
            Almacen sur = new Almacen();
            sur.setNombre("Almacén Sur");
            sur.setUbicacion("Sevilla");
            sur.setCapacidadMaxima(3000);
            sur.setStockActual(0);
            sur.setPrioritario(false);

            almacenRepository.saveAll(List.of(central, norte, sur));
            log.info(" Cargados 3 almacenes de muestra: 1 prioritario + 2 normales");
        };
    }
}
