package com.ssline.sell.supermarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssline.sell.supermarket.dto.ProductoDTO;
import com.ssline.sell.supermarket.exception.NotFoundException;
import com.ssline.sell.supermarket.services.IProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IProductoService productoService;

    private ProductoDTO productoDTO;

    @BeforeEach
    void setUp() {
        productoDTO = ProductoDTO.builder()
                .id(1L).nombre("Leche").categoria("Lacteos").precio(1500.0).cantidad(10).build();
    }

    // ── GET /api/productos ────────────────────────────────────────────────────

    @Test
    void GET_productos_retorna_200_y_lista() throws Exception {
        when(productoService.traerProductos()).thenReturn(List.of(productoDTO));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Leche"))
                .andExpect(jsonPath("$[0].categoria").value("Lacteos"))
                .andExpect(jsonPath("$[0].precio").value(1500.0))
                .andExpect(jsonPath("$[0].cantidad").value(10));
    }

    @Test
    void GET_productos_lista_vacia_retorna_200_array_vacio() throws Exception {
        when(productoService.traerProductos()).thenReturn(List.of());

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── POST /api/productos ───────────────────────────────────────────────────

    @Test
    void POST_producto_retorna_201_y_dto_creado() throws Exception {
        when(productoService.crearProducto(any(ProductoDTO.class))).thenReturn(productoDTO);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Leche"));
    }

    @Test
    void POST_producto_incluye_header_location() throws Exception {
        when(productoService.crearProducto(any(ProductoDTO.class))).thenReturn(productoDTO);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    // ── PUT /api/productos/{id} ───────────────────────────────────────────────

    @Test
    void PUT_producto_retorna_200_y_dto_actualizado() throws Exception {
        ProductoDTO actualizado = ProductoDTO.builder()
                .id(1L).nombre("Leche Entera").categoria("Lacteos").precio(2000.0).cantidad(5).build();
        when(productoService.actualizarProducto(eq(1L), any(ProductoDTO.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Leche Entera"))
                .andExpect(jsonPath("$.precio").value(2000.0));
    }

    @Test
    void PUT_producto_no_encontrado_retorna_404() throws Exception {
        when(productoService.actualizarProducto(eq(99L), any(ProductoDTO.class)))
                .thenThrow(new NotFoundException("Producto no encontrado con id: 99"));

        mockMvc.perform(put("/api/productos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoDTO)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/productos/{id} ────────────────────────────────────────────

    @Test
    void DELETE_producto_retorna_204() throws Exception {
        doNothing().when(productoService).eliminarProducto(1L);

        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());

        verify(productoService).eliminarProducto(1L);
    }

    @Test
    void DELETE_producto_no_encontrado_retorna_404() throws Exception {
        doThrow(new NotFoundException("Producto no encontrado con id: 99"))
                .when(productoService).eliminarProducto(99L);

        mockMvc.perform(delete("/api/productos/99"))
                .andExpect(status().isNotFound());
    }
}
