package com.example.inventarioapiad.repository;

import com.example.inventarioapiad.entity.Almacen;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlmacenRepository extends CrudRepository<Almacen, Long> {
}