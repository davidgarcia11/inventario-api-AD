package com.example.inventarioapiad.repository;

import com.example.inventarioapiad.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

// Repositorio de Producto.
//
// Extiende CrudRepository (operaciones básicas que ya usábamos en la V1)
// y PagingAndSortingRepository, que añade el método findAll(Pageable)
// que devuelve un Page<Producto>. Esto lo usa el endpoint V2 paginado.
@Repository
public interface ProductoRepository extends CrudRepository<Producto, Long>,
        PagingAndSortingRepository<Producto, Long> {

    // Heredado de PagingAndSortingRepository
    Page<Producto> findAll(Pageable pageable);
}