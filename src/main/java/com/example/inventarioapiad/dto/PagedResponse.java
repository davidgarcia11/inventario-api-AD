package com.example.inventarioapiad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

// DTO genérico para devolver respuestas paginadas en los endpoints V2.
// Encapsula el Page<T> de Spring para no exponer detalles internos como
// 'pageable' o 'sort' y devolver solo la información que necesita el cliente.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    // Constructor de conveniencia para transformar un Page<T> de Spring
    // en nuestro PagedResponse<T>.
    public static <T> PagedResponse<T> desde(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
