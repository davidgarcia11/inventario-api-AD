package com.example.inventarioapiad.service;

import com.example.inventarioapiad.entity.Venta;
import com.example.inventarioapiad.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    public Venta crear(Venta venta) {
        if (venta.getCliente() == null) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }
        if (venta.getProducto() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (venta.getAlmacen() == null) {
            throw new IllegalArgumentException("El almacén es obligatorio");
        }
        if (venta.getCantidad() == null || venta.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (venta.getPrecioUnitario() == null || venta.getPrecioUnitario() <= 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
        }
        if (venta.getFechaVenta() == null) {
            throw new IllegalArgumentException("La fecha de venta es obligatoria");
        }
        if (venta.getNumeroPedido() == null || venta.getNumeroPedido().isBlank()) {
            throw new IllegalArgumentException("El número de pedido es obligatorio");
        }
        return ventaRepository.save(venta);
    }

    public Venta buscarPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El ID debe ser válido");
        }
        return ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
    }

    public List<Venta> buscarTodos() {
        return (List<Venta>) ventaRepository.findAll();
    }

    public Venta actualizar(Long id, Venta ventaActualizada) {
        Venta venta = buscarPorId(id);

        if (ventaActualizada.getCliente() != null) {
            venta.setCliente(ventaActualizada.getCliente());
        }
        if (ventaActualizada.getProducto() != null) {
            venta.setProducto(ventaActualizada.getProducto());
        }
        if (ventaActualizada.getAlmacen() != null) {
            venta.setAlmacen(ventaActualizada.getAlmacen());
        }
        if (ventaActualizada.getCantidad() != null && ventaActualizada.getCantidad() > 0) {
            venta.setCantidad(ventaActualizada.getCantidad());
        }
        if (ventaActualizada.getPrecioUnitario() != null && ventaActualizada.getPrecioUnitario() > 0) {
            venta.setPrecioUnitario(ventaActualizada.getPrecioUnitario());
        }
        if (ventaActualizada.getFechaVenta() != null) {
            venta.setFechaVenta(ventaActualizada.getFechaVenta());
        }
        if (ventaActualizada.getNumeroPedido() != null) {
            venta.setNumeroPedido(ventaActualizada.getNumeroPedido());
        }
        if (ventaActualizada.getEstado() != null) {
            venta.setEstado(ventaActualizada.getEstado());
        }

        return ventaRepository.save(venta);
    }

    public void eliminar(Long id) {
        Venta venta = buscarPorId(id);
        venta.setEstado("CANCELADA");
        ventaRepository.save(venta);
    }
}