package com.ssline.sell.supermarket.mapper;

import com.ssline.sell.supermarket.dto.ProductoDTO;
import com.ssline.sell.supermarket.dto.SucursalDTO;
import com.ssline.sell.supermarket.dto.VentaDTO;
import com.ssline.sell.supermarket.model.DetalleVenta;
import com.ssline.sell.supermarket.model.Producto;
import com.ssline.sell.supermarket.model.Sucursal;
import com.ssline.sell.supermarket.model.Venta;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    // ── Producto ──────────────────────────────────────────────────────────────

    @Test
    void toDTO_producto_null_retorna_null() {
        assertThat(Mapper.toDTO((Producto) null)).isNull();
    }

    @Test
    void toDTO_producto_mapea_todos_los_campos() {
        Producto p = Producto.builder()
                .id(1L).nombre("Leche").categoria("Lacteos").precio(1500.0).cantidad(10).build();

        ProductoDTO dto = Mapper.toDTO(p);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNombre()).isEqualTo("Leche");
        assertThat(dto.getCategoria()).isEqualTo("Lacteos");
        assertThat(dto.getPrecio()).isEqualTo(1500.0);
        assertThat(dto.getCantidad()).isEqualTo(10);
    }

    // ── Sucursal ──────────────────────────────────────────────────────────────

    @Test
    void toDTO_sucursal_null_retorna_null() {
        assertThat(Mapper.toDTO((Sucursal) null)).isNull();
    }

    @Test
    void toDTO_sucursal_mapea_todos_los_campos() {
        Sucursal s = Sucursal.builder()
                .id(2L).nombre("Centro").direccion("Av. Principal 123").build();

        SucursalDTO dto = Mapper.toDTO(s);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getNombre()).isEqualTo("Centro");
        assertThat(dto.getDireccion()).isEqualTo("Av. Principal 123");
    }

    // ── Venta ─────────────────────────────────────────────────────────────────

    @Test
    void toDTO_venta_null_retorna_null() {
        assertThat(Mapper.toDTO((Venta) null)).isNull();
    }

    @Test
    void toDTO_venta_con_detalle_calcula_total_correctamente() {
        Sucursal suc = Sucursal.builder().id(1L).nombre("Norte").direccion("Calle 1").build();
        Producto prod = Producto.builder()
                .id(1L).nombre("Pan").categoria("Panaderia").precio(500.0).cantidad(5).build();

        DetalleVenta det = new DetalleVenta();
        det.setProducto(prod);
        det.setCantProd(3);
        det.setPrecio(500.0);

        Venta v = new Venta();
        v.setId(1L);
        v.setFecha(LocalDate.of(2024, 6, 15));
        v.setEstado("COMPLETADO");
        v.setTotal(0.0);
        v.setSucursal(suc);
        v.setDetalle(List.of(det));

        VentaDTO dto = Mapper.toDTO(v);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFecha()).isEqualTo(LocalDate.of(2024, 6, 15));
        assertThat(dto.getEstado()).isEqualTo("COMPLETADO");
        assertThat(dto.getSucursalId()).isEqualTo(1L);
        assertThat(dto.getDetalle()).hasSize(1);
        assertThat(dto.getTotal()).isEqualTo(1500.0); // 500 * 3
        assertThat(dto.getDetalle().get(0).getNombreProducto()).isEqualTo("Pan");
        assertThat(dto.getDetalle().get(0).getCantProd()).isEqualTo(3);
        assertThat(dto.getDetalle().get(0).getPrecio()).isEqualTo(500.0);
        assertThat(dto.getDetalle().get(0).getSubtotal()).isEqualTo(1500.0);
        assertThat(dto.getDetalle().get(0).getId()).isEqualTo(1L); // id del producto
    }

    @Test
    void toDTO_venta_sin_detalle_total_es_cero() {
        Sucursal suc = Sucursal.builder().id(1L).nombre("Sur").direccion("Calle 2").build();

        Venta v = new Venta();
        v.setId(2L);
        v.setFecha(LocalDate.now());
        v.setEstado("PENDIENTE");
        v.setTotal(0.0);
        v.setSucursal(suc);
        v.setDetalle(new ArrayList<>());

        VentaDTO dto = Mapper.toDTO(v);

        assertThat(dto.getDetalle()).isEmpty();
        assertThat(dto.getTotal()).isEqualTo(0.0);
    }

    @Test
    void toDTO_venta_multiples_detalles_acumula_total() {
        Sucursal suc = Sucursal.builder().id(1L).nombre("Este").direccion("Calle 3").build();
        Producto p1 = Producto.builder().id(1L).nombre("Pan").categoria("X").precio(100.0).cantidad(1).build();
        Producto p2 = Producto.builder().id(2L).nombre("Leche").categoria("Y").precio(200.0).cantidad(1).build();

        DetalleVenta d1 = new DetalleVenta();
        d1.setProducto(p1);
        d1.setCantProd(2);
        d1.setPrecio(100.0);

        DetalleVenta d2 = new DetalleVenta();
        d2.setProducto(p2);
        d2.setCantProd(3);
        d2.setPrecio(200.0);

        Venta v = new Venta();
        v.setId(3L);
        v.setFecha(LocalDate.now());
        v.setEstado("PENDIENTE");
        v.setSucursal(suc);
        v.setDetalle(List.of(d1, d2));

        VentaDTO dto = Mapper.toDTO(v);

        // 100*2 + 200*3 = 200 + 600 = 800
        assertThat(dto.getTotal()).isEqualTo(800.0);
        assertThat(dto.getDetalle()).hasSize(2);
    }
}
