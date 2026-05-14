package com.ssline.sell.supermarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssline.sell.supermarket.dto.SucursalDTO;
import com.ssline.sell.supermarket.exception.NotFoundException;
import com.ssline.sell.supermarket.services.ISucursalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SucursalController.class)
class SucursalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ISucursalService sucursalService;

    private SucursalDTO sucursalDTO;

    @BeforeEach
    void setUp() {
        sucursalDTO = SucursalDTO.builder()
                .id(1L).nombre("Centro").direccion("Av. Principal 123").build();
    }

    // ── GET /api/sucursales ───────────────────────────────────────────────────

    @Test
    void GET_sucursales_retorna_200_y_lista() throws Exception {
        when(sucursalService.getAllSucursales()).thenReturn(List.of(sucursalDTO));

        mockMvc.perform(get("/api/sucursales"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Centro"))
                .andExpect(jsonPath("$[0].direccion").value("Av. Principal 123"));
    }

    @Test
    void GET_sucursales_lista_vacia_retorna_200_array_vacio() throws Exception {
        when(sucursalService.getAllSucursales()).thenReturn(List.of());

        mockMvc.perform(get("/api/sucursales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── POST /api/sucursales ──────────────────────────────────────────────────

    @Test
    void POST_sucursal_retorna_201_y_dto_creado() throws Exception {
        when(sucursalService.createSucursal(any(SucursalDTO.class))).thenReturn(sucursalDTO);

        mockMvc.perform(post("/api/sucursales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sucursalDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Centro"));
    }

    @Test
    void POST_sucursal_incluye_header_location() throws Exception {
        when(sucursalService.createSucursal(any(SucursalDTO.class))).thenReturn(sucursalDTO);

        mockMvc.perform(post("/api/sucursales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sucursalDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/sucursales/1"));
    }

    // ── PUT /api/sucursales/{id} ──────────────────────────────────────────────

    @Test
    void PUT_sucursal_retorna_200_y_dto_actualizado() throws Exception {
        SucursalDTO actualizada = SucursalDTO.builder()
                .id(1L).nombre("Norte").direccion("Calle 99").build();
        when(sucursalService.updateSucursal(eq(1L), any(SucursalDTO.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/sucursales/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizada)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Norte"))
                .andExpect(jsonPath("$.direccion").value("Calle 99"));
    }

    @Test
    void PUT_sucursal_no_encontrada_retorna_404() throws Exception {
        when(sucursalService.updateSucursal(eq(99L), any(SucursalDTO.class)))
                .thenThrow(new NotFoundException("Sucursal no encontrada"));

        mockMvc.perform(put("/api/sucursales/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sucursalDTO)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/sucursales/{id} ───────────────────────────────────────────

    @Test
    void DELETE_sucursal_retorna_204() throws Exception {
        doNothing().when(sucursalService).eliminarSucursal(1L);

        mockMvc.perform(delete("/api/sucursales/1"))
                .andExpect(status().isNoContent());

        verify(sucursalService).eliminarSucursal(1L);
    }

    @Test
    void DELETE_sucursal_no_encontrada_retorna_404() throws Exception {
        doThrow(new NotFoundException("Sucursal no encontrada"))
                .when(sucursalService).eliminarSucursal(99L);

        mockMvc.perform(delete("/api/sucursales/99"))
                .andExpect(status().isNotFound());
    }
}
