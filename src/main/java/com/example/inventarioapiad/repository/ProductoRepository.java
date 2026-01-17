package com.example.inventarioapiad.repository;

import com.example.inventarioapiad.entity.Producto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends CrudRepository<Producto, Long> {
    // Spring Data JPA genera automáticamente:
    // - save()
    // - findById()
    // - findAll()
    // - deleteById()
    // - count()
    // ... y más
}