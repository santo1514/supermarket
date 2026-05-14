package com.ssline.sell.supermarket.services;

import com.ssline.sell.supermarket.dto.ProductoDTO;
import com.ssline.sell.supermarket.exception.NotFoundException;
import com.ssline.sell.supermarket.model.Producto;
import com.ssline.sell.supermarket.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        producto = Producto.builder()
                .id(1L).nombre("Leche").categoria("Lacteos").precio(1500.0).cantidad(10).build();
        productoDTO = ProductoDTO.builder()
                .id(1L).nombre("Leche").categoria("Lacteos").precio(1500.0).cantidad(10).build();
    }

    // ── traerProductos ────────────────────────────────────────────────────────

    @Test
    void traerProductos_lista_vacia_retorna_lista_vacia() {
        when(productoRepository.findAll()).thenReturn(List.of());

        List<ProductoDTO> result = productoService.traerProductos();

        assertThat(result).isEmpty();
        verify(productoRepository).findAll();
    }

    @Test
    void traerProductos_retorna_lista_mapeada() {
        when(productoRepository.findAll()).thenReturn(List.of(producto));

        List<ProductoDTO> result = productoService.traerProductos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getNombre()).isEqualTo("Leche");
        assertThat(result.get(0).getCategoria()).isEqualTo("Lacteos");
        assertThat(result.get(0).getPrecio()).isEqualTo(1500.0);
        assertThat(result.get(0).getCantidad()).isEqualTo(10);
    }

    @Test
    void traerProductos_multiples_productos_retorna_todos() {
        Producto otro = Producto.builder().id(2L).nombre("Pan").categoria("Panaderia").precio(300.0).cantidad(5).build();
        when(productoRepository.findAll()).thenReturn(List.of(producto, otro));

        List<ProductoDTO> result = productoService.traerProductos();

        assertThat(result).hasSize(2);
    }

    // ── crearProducto ─────────────────────────────────────────────────────────

    @Test
    void crearProducto_guarda_y_retorna_dto_mapeado() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        ProductoDTO result = productoService.crearProducto(productoDTO);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Leche");
        assertThat(result.getCategoria()).isEqualTo("Lacteos");
        assertThat(result.getPrecio()).isEqualTo(1500.0);
        assertThat(result.getCantidad()).isEqualTo(10);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crearProducto_no_usa_id_del_dto_al_construir_entidad() {
        // el id lo asigna la BD, no el DTO
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        productoService.crearProducto(productoDTO);

        verify(productoRepository).save(argThat(p -> p.getId() == null));
    }

    // ── actualizarProducto ────────────────────────────────────────────────────

    @Test
    void actualizarProducto_no_encontrado_lanza_NotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.actualizarProducto(99L, productoDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizarProducto_encontrado_actualiza_todos_los_campos() {
        ProductoDTO datosNuevos = ProductoDTO.builder()
                .nombre("Leche Entera").categoria("Lacteos Premium").precio(2000.0).cantidad(5).build();
        Producto productoActualizado = Producto.builder()
                .id(1L).nombre("Leche Entera").categoria("Lacteos Premium").precio(2000.0).cantidad(5).build();

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActualizado);

        ProductoDTO result = productoService.actualizarProducto(1L, datosNuevos);

        assertThat(result.getNombre()).isEqualTo("Leche Entera");
        assertThat(result.getCategoria()).isEqualTo("Lacteos Premium");
        assertThat(result.getPrecio()).isEqualTo(2000.0);
        assertThat(result.getCantidad()).isEqualTo(5);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void actualizarProducto_modifica_la_entidad_antes_de_guardar() {
        ProductoDTO datosNuevos = ProductoDTO.builder()
                .nombre("Pan").categoria("Panaderia").precio(300.0).cantidad(20).build();

        when(productoRepository.findById(eq(1L))).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        productoService.actualizarProducto(1L, datosNuevos);

        verify(productoRepository).save(argThat(p ->
                p.getNombre().equals("Pan") &&
                p.getCategoria().equals("Panaderia") &&
                p.getPrecio().equals(300.0) &&
                p.getCantidad().equals(20)
        ));
    }

    // ── eliminarProducto ──────────────────────────────────────────────────────

    @Test
    void eliminarProducto_no_encontrado_lanza_NotFoundException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.eliminarProducto(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(productoRepository, never()).delete(any());
    }

    @Test
    void eliminarProducto_encontrado_invoca_delete() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        productoService.eliminarProducto(1L);

        verify(productoRepository).delete(producto);
    }
}
