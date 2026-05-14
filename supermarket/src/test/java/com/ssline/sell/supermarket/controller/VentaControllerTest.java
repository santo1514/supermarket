package com.ssline.sell.supermarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssline.sell.supermarket.dto.DetalleVentaDTO;
import com.ssline.sell.supermarket.dto.VentaDTO;
import com.ssline.sell.supermarket.exception.NotFoundException;
import com.ssline.sell.supermarket.services.IVentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IVentaService ventaService;

    private VentaDTO ventaDTO;

    @BeforeEach
    void setUp() {
        DetalleVentaDTO detalle = DetalleVentaDTO.builder()
                .id(1L).nombreProducto("Pan").cantProd(2).precio(500.0).subtotal(1000.0).build();

        ventaDTO = VentaDTO.builder()
                .id(1L)
                .fecha(LocalDate.of(2024, 6, 1))
                .estado("PENDIENTE")
                .sucursalId(1L)
                .detalle(List.of(detalle))
                .total(1000.0)
                .build();
    }

    // ── GET /api/ventas ───────────────────────────────────────────────────────

    @Test
    void GET_ventas_retorna_200_y_lista() throws Exception {
        when(ventaService.traerVentas()).thenReturn(List.of(ventaDTO));

        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].sucursalId").value(1))
                .andExpect(jsonPath("$[0].total").value(1000.0));
    }

    @Test
    void GET_ventas_lista_vacia_retorna_200_array_vacio() throws Exception {
        when(ventaService.traerVentas()).thenReturn(List.of());

        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── POST /api/ventas ──────────────────────────────────────────────────────

    @Test
    void POST_venta_retorna_201_y_dto_creado() throws Exception {
        when(ventaService.crearVenta(any(VentaDTO.class))).thenReturn(ventaDTO);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.total").value(1000.0));
    }

    @Test
    void POST_venta_incluye_header_location() throws Exception {
        when(ventaService.crearVenta(any(VentaDTO.class))).thenReturn(ventaDTO);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ventas/1"));
    }

    @Test
    void POST_venta_datos_invalidos_retorna_500() throws Exception {
        when(ventaService.crearVenta(any(VentaDTO.class)))
                .thenThrow(new RuntimeException("Debe incluir al menos un producto"));

        VentaDTO sinDetalle = VentaDTO.builder().sucursalId(1L).detalle(List.of()).build();

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sinDetalle)))
                .andExpect(status().isInternalServerError());
    }

    // ── PUT /api/ventas/{id} ──────────────────────────────────────────────────

    @Test
    void PUT_venta_retorna_200_y_dto_actualizado() throws Exception {
        VentaDTO actualizada = VentaDTO.builder()
                .id(1L).estado("COMPLETADO").sucursalId(1L).total(2000.0)
                .detalle(List.of()).build();
        when(ventaService.actualizarVenta(eq(1L), any(VentaDTO.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/ventas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADO"))
                .andExpect(jsonPath("$.total").value(2000.0));
    }

    @Test
    void PUT_venta_no_encontrada_retorna_500() throws Exception {
        when(ventaService.actualizarVenta(eq(99L), any(VentaDTO.class)))
                .thenThrow(new RuntimeException("Venta no encontrada"));

        mockMvc.perform(put("/api/ventas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaDTO)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void PUT_venta_sucursal_no_encontrada_retorna_404() throws Exception {
        when(ventaService.actualizarVenta(eq(1L), any(VentaDTO.class)))
                .thenThrow(new NotFoundException("Sucursal no encontrada"));

        mockMvc.perform(put("/api/ventas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaDTO)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/ventas/{id} ───────────────────────────────────────────────

    @Test
    void DELETE_venta_retorna_204() throws Exception {
        doNothing().when(ventaService).eliminarVenta(1L);

        mockMvc.perform(delete("/api/ventas/1"))
                .andExpect(status().isNoContent());

        verify(ventaService).eliminarVenta(1L);
    }

    @Test
    void DELETE_venta_no_encontrada_retorna_500() throws Exception {
        doThrow(new RuntimeException("Venta no encontrada"))
                .when(ventaService).eliminarVenta(99L);

        mockMvc.perform(delete("/api/ventas/99"))
                .andExpect(status().isInternalServerError());
    }
}
