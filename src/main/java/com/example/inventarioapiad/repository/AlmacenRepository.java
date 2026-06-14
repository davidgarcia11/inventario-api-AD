package com.example.inventarioapiad.repository;

import com.example.inventarioapiad.entity.Almacen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

// Repositorio de Almacén.
//
// Extiende CrudRepository (lo que ya usábamos en V1) y también
// PagingAndSortingRepository para que la V2 pueda devolver páginas con
// metadatos (page, size, totalElements, totalPages).
@Repository
public interface AlmacenRepository extends CrudRepository<Almacen, Long>,
        PagingAndSortingRepository<Almacen, Long> {

    // Heredado de PagingAndSortingRepository
    Page<Almacen> findAll(Pageable pageable);
}
