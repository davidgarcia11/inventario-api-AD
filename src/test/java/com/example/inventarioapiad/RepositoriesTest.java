package com.example.inventarioapiad;

import com.example.inventarioapiad.entity.*;
import com.example.inventarioapiad.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RepositoriesTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private AlmacenRepository almacenRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Test
    public void testGuardarProducto() {
        Producto p = new Producto();
        p.setNombre("Tornillo M10");
        p.setSku("TORNILLO-M10");
        p.setPrecioVenta(1.25f);
        p.setStockTotal(100);

        Producto guardado = productoRepository.save(p);

        assertNotNull(guardado.getId());
        assertEquals("Tornillo M10", guardado.getNombre());
    }

    @Test
    public void testGuardarAlmacen() {
        Almacen a = new Almacen();
        a.setNombre("Almacén Central");
        a.setUbicacion("Barcelona");
        a.setCapacidadMaxima(10000);
        a.setStockActual(5000);

        Almacen guardado = almacenRepository.save(a);

        assertNotNull(guardado.getId());
        assertEquals("Almacén Central", guardado.getNombre());
    }

    @Test
    public void testGuardarProveedor() {
        Proveedor pr = new Proveedor();
        pr.setNombre("Aceros García");
        pr.setEmail("contacto@acerosgarcia.com");
        pr.setDiasEntrega(3);

        Proveedor guardado = proveedorRepository.save(pr);

        assertNotNull(guardado.getId());
        assertEquals("Aceros García", guardado.getNombre());
    }

    @Test
    public void testGuardarCliente() {
        Cliente c = new Cliente();
        c.setNombre("Constructora López");
        c.setEmail("ventas@constructoralopez.com");
        c.setCiudad("Barcelona");

        Cliente guardado = clienteRepository.save(c);

        assertNotNull(guardado.getId());
        assertEquals("Constructora López", guardado.getNombre());
    }

    @Test
    public void testGuardarCompra() {
        Producto producto = new Producto();
        producto.setNombre("Tuerca M10");
        producto.setSku("TUERCA-M10");
        producto.setPrecioVenta(0.75f);
        producto.setStockTotal(200);
        Producto productoGuardado = productoRepository.save(producto);

        Almacen almacen = new Almacen();
        almacen.setNombre("Almacén Madrid");
        almacen.setUbicacion("Madrid");
        almacen.setCapacidadMaxima(8000);
        Almacen almacenGuardado = almacenRepository.save(almacen);

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre("Tornillos España");
        proveedor.setEmail("info@tornillosespana.com");
        proveedor.setDiasEntrega(2);
        Proveedor proveedorGuardado = proveedorRepository.save(proveedor);

        Compra compra = new Compra();
        compra.setProveedor(proveedorGuardado);
        compra.setProducto(productoGuardado);
        compra.setAlmacen(almacenGuardado);
        compra.setCantidad(500);
        compra.setPrecioUnitario(0.50f);
        compra.setFechaCompra(java.time.LocalDateTime.now());
        compra.setNumeroFactura("FAC-2025-001");
        compra.setEstado("RECIBIDA");

        Compra compraGuardada = compraRepository.save(compra);

        assertNotNull(compraGuardada.getId());
        assertEquals(500, compraGuardada.getCantidad());
        assertEquals("Tornillos España", compraGuardada.getProveedor().getNombre());
    }

    @Test
    public void testGuardarVenta() {
        Producto producto = new Producto();
        producto.setNombre("Arandela M10");
        producto.setSku("ARANDELA-M10");
        producto.setPrecioVenta(0.50f);
        producto.setStockTotal(300);
        Producto productoGuardado = productoRepository.save(producto);

        Almacen almacen = new Almacen();
        almacen.setNombre("Almacén Valencia");
        almacen.setUbicacion("Valencia");
        almacen.setCapacidadMaxima(7000);
        Almacen almacenGuardado = almacenRepository.save(almacen);

        Cliente cliente = new Cliente();
        cliente.setNombre("Empresa Construcción S.L.");
        cliente.setEmail("compras@empresaconstruccion.com");
        cliente.setCiudad("Valencia");
        Cliente clienteGuardado = clienteRepository.save(cliente);

        Venta venta = new Venta();
        venta.setCliente(clienteGuardado);
        venta.setProducto(productoGuardado);
        venta.setAlmacen(almacenGuardado);
        venta.setCantidad(150);
        venta.setPrecioUnitario(0.60f);
        venta.setFechaVenta(java.time.LocalDateTime.now());
        venta.setNumeroPedido("PED-2025-0001");
        venta.setEstado("ENTREGADA");

        Venta ventaGuardada = ventaRepository.save(venta);

        assertNotNull(ventaGuardada.getId());
        assertEquals(150, ventaGuardada.getCantidad());
        assertEquals("Empresa Construcción S.L.", ventaGuardada.getCliente().getNombre());
    }
}