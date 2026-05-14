package com.ssline.sell.supermarket.services;

import com.ssline.sell.supermarket.dto.DetalleVentaDTO;
import com.ssline.sell.supermarket.dto.VentaDTO;
import com.ssline.sell.supermarket.exception.NotFoundException;
import com.ssline.sell.supermarket.model.DetalleVenta;
import com.ssline.sell.supermarket.model.Producto;
import com.ssline.sell.supermarket.model.Sucursal;
import com.ssline.sell.supermarket.model.Venta;
import com.ssline.sell.supermarket.repository.ProductoRepository;
import com.ssline.sell.supermarket.repository.SucursalRepository;
import com.ssline.sell.supermarket.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepo;
    @Mock private ProductoRepository productoRepo;
    @Mock private SucursalRepository sucursalRepo;

    @InjectMocks
    private VentaService ventaService;

    private Sucursal sucursal;
    private Producto producto;
    private Venta venta;
    private VentaDTO ventaDTO;
    private DetalleVentaDTO detalleDTO;

    @BeforeEach
    void setUp() {
        sucursal = Sucursal.builder().id(1L).nombre("Centro").direccion("Calle 1").build();
        producto = Producto.builder().id(1L).nombre("Pan").categoria("Panaderia").precio(500.0).cantidad(10).build();

        detalleDTO = DetalleVentaDTO.builder()
                .nombreProducto("Pan").cantProd(2).precio(500.0).subtotal(1000.0).build();

        ventaDTO = VentaDTO.builder()
                .sucursalId(1L)
                .fecha(LocalDate.of(2024, 6, 1))
                .estado("PENDIENTE")
                .total(1000.0)
                .detalle(List.of(detalleDTO))
                .build();

        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);
        detalle.setProducto(producto);
        detalle.setCantProd(2);
        detalle.setPrecio(500.0);

        venta = new Venta();
        venta.setId(1L);
        venta.setFecha(LocalDate.of(2024, 6, 1));
        venta.setEstado("PENDIENTE");
        venta.setTotal(1000.0);
        venta.setSucursal(sucursal);
        venta.setDetalle(new ArrayList<>(List.of(detalle)));
        detalle.setVenta(venta);
    }

    // ── traerVentas ───────────────────────────────────────────────────────────

    @Test
    void traerVentas_lista_vacia_retorna_lista_vacia() {
        when(ventaRepo.findAll()).thenReturn(List.of());

        List<VentaDTO> result = ventaService.traerVentas();

        assertThat(result).isEmpty();
        verify(ventaRepo).findAll();
    }

    @Test
    void traerVentas_retorna_lista_mapeada() {
        when(ventaRepo.findAll()).thenReturn(List.of(venta));

        List<VentaDTO> result = ventaService.traerVentas();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getEstado()).isEqualTo("PENDIENTE");
        assertThat(result.get(0).getSucursalId()).isEqualTo(1L);
        assertThat(result.get(0).getDetalle()).hasSize(1);
    }

    @Test
    void traerVentas_multiples_ventas_retorna_todas() {
        Venta otraVenta = new Venta();
        otraVenta.setId(2L);
        otraVenta.setEstado("COMPLETADO");
        otraVenta.setSucursal(sucursal);
        otraVenta.setDetalle(new ArrayList<>());

        when(ventaRepo.findAll()).thenReturn(List.of(venta, otraVenta));

        List<VentaDTO> result = ventaService.traerVentas();

        assertThat(result).hasSize(2);
    }

    // ── crearVenta ────────────────────────────────────────────────────────────

    @Test
    void crearVenta_dto_null_lanza_RuntimeException() {
        assertThatThrownBy(() -> ventaService.crearVenta(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("VentaDTO es null");
    }

    @Test
    void crearVenta_sucursalId_null_lanza_RuntimeException() {
        ventaDTO.setSucursalId(null);

        assertThatThrownBy(() -> ventaService.crearVenta(ventaDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Debe indicar la sucursal");
    }

    @Test
    void crearVenta_detalle_null_lanza_RuntimeException() {
        ventaDTO.setDetalle(null);

        assertThatThrownBy(() -> ventaService.crearVenta(ventaDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Debe incluir al menos un producto");
    }

    @Test
    void crearVenta_detalle_vacio_lanza_RuntimeException() {
        ventaDTO.setDetalle(List.of());

        assertThatThrownBy(() -> ventaService.crearVenta(ventaDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Debe incluir al menos un producto");
    }

    @Test
    void crearVenta_sucursal_no_encontrada_lanza_NotFoundException() {
        when(sucursalRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.crearVenta(ventaDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Sucursal no encontrada");

        verify(ventaRepo, never()).save(any());
    }

    @Test
    void crearVenta_producto_no_encontrado_lanza_RuntimeException() {
        when(sucursalRepo.findById(1L)).thenReturn(Optional.of(sucursal));
        when(productoRepo.findByNombre("Pan")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.crearVenta(ventaDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pan");

        verify(ventaRepo, never()).save(any());
    }

    @Test
    void crearVenta_exitoso_guarda_y_retorna_dto() {
        when(sucursalRepo.findById(1L)).thenReturn(Optional.of(sucursal));
        when(productoRepo.findByNombre("Pan")).thenReturn(Optional.of(producto));
        when(ventaRepo.save(any(Venta.class))).thenReturn(venta);

        VentaDTO result = ventaService.crearVenta(ventaDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSucursalId()).isEqualTo(1L);
        assertThat(result.getDetalle()).hasSize(1);
        verify(ventaRepo).save(any(Venta.class));
    }

    @Test
    void crearVenta_asocia_sucursal_y_productos_correctamente() {
        when(sucursalRepo.findById(1L)).thenReturn(Optional.of(sucursal));
        when(productoRepo.findByNombre("Pan")).thenReturn(Optional.of(producto));
        when(ventaRepo.save(any(Venta.class))).thenReturn(venta);

        ventaService.crearVenta(ventaDTO);

        verify(ventaRepo).save(argThat(v ->
                v.getSucursal().equals(sucursal) &&
                v.getDetalle().size() == 1 &&
                v.getDetalle().get(0).getProducto().equals(producto)
        ));
    }

    // ── actualizarVenta ───────────────────────────────────────────────────────

    @Test
    void actualizarVenta_no_encontrada_lanza_RuntimeException() {
        when(ventaRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.actualizarVenta(99L, ventaDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Venta no encontrada");

        verify(ventaRepo, never()).save(any());
    }

    @Test
    void actualizarVenta_actualiza_fecha_cuando_no_es_null() {
        LocalDate nuevaFecha = LocalDate.of(2025, 1, 1);
        VentaDTO parcial = VentaDTO.builder().fecha(nuevaFecha).build();

        when(ventaRepo.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepo.save(any(Venta.class))).thenReturn(venta);

        ventaService.actualizarVenta(1L, parcial);

        assertThat(venta.getFecha()).isEqualTo(nuevaFecha);
    }

    @Test
    void actualizarVenta_actualiza_estado_cuando_no_es_null() {
        VentaDTO parcial = VentaDTO.builder().estado("COMPLETADO").build();

        when(ventaRepo.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepo.save(any(Venta.class))).thenReturn(venta);

        ventaService.actualizarVenta(1L, parcial);

        assertThat(venta.getEstado()).isEqualTo("COMPLETADO");
    }

    @Test
    void actualizarVenta_actualiza_total_cuando_no_es_null() {
        VentaDTO parcial = VentaDTO.builder().total(9999.0).build();

        when(ventaRepo.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepo.save(any(Venta.class))).thenReturn(venta);

        ventaService.actualizarVenta(1L, parcial);

        assertThat(venta.getTotal()).isEqualTo(9999.0);
    }

    @Test
    void actualizarVenta_campos_null_no_modifica_venta() {
        LocalDate fechaOriginal = venta.getFecha();
        String estadoOriginal = venta.getEstado();
        Double totalOriginal = venta.getTotal();

        when(ventaRepo.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepo.save(any(Venta.class))).thenReturn(venta);

        ventaService.actualizarVenta(1L, new VentaDTO());

        assertThat(venta.getFecha()).isEqualTo(fechaOriginal);
        assertThat(venta.getEstado()).isEqualTo(estadoOriginal);
        assertThat(venta.getTotal()).isEqualTo(totalOriginal);
    }

    @Test
    void actualizarVenta_cambia_sucursal_cuando_sucursalId_no_es_null() {
        Sucursal nuevaSucursal = Sucursal.builder().id(2L).nombre("Norte").direccion("Calle 2").build();
        VentaDTO parcial = VentaDTO.builder().sucursalId(2L).build();

        when(ventaRepo.findById(1L)).thenReturn(Optional.of(venta));
        when(sucursalRepo.findById(2L)).thenReturn(Optional.of(nuevaSucursal));
        when(ventaRepo.save(any(Venta.class))).thenReturn(venta);

        ventaService.actualizarVenta(1L, parcial);

        assertThat(venta.getSucursal()).isEqualTo(nuevaSucursal);
        verify(sucursalRepo).findById(2L);
    }

    @Test
    void actualizarVenta_sucursal_nueva_no_encontrada_lanza_NotFoundException() {
        VentaDTO parcial = VentaDTO.builder().sucursalId(99L).build();

        when(ventaRepo.findById(1L)).thenReturn(Optional.of(venta));
        when(sucursalRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.actualizarVenta(1L, parcial))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Sucursal no encontrada");
    }

    @Test
    void actualizarVenta_retorna_dto_mapeado() {
        when(ventaRepo.findById(1L)).thenReturn(Optional.of(venta));
        when(ventaRepo.save(any(Venta.class))).thenReturn(venta);

        VentaDTO result = ventaService.actualizarVenta(1L, new VentaDTO());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSucursalId()).isEqualTo(1L);
    }

    // ── eliminarVenta ─────────────────────────────────────────────────────────

    @Test
    void eliminarVenta_no_encontrada_lanza_RuntimeException() {
        when(ventaRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.eliminarVenta(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Venta no encontrada");

        verify(ventaRepo, never()).delete(any());
    }

    @Test
    void eliminarVenta_encontrada_invoca_delete() {
        when(ventaRepo.findById(1L)).thenReturn(Optional.of(venta));

        ventaService.eliminarVenta(1L);

        verify(ventaRepo).delete(venta);
    }
}
