package com.ssline.sell.supermarket.services;

import com.ssline.sell.supermarket.dto.SucursalDTO;
import com.ssline.sell.supermarket.exception.NotFoundException;
import com.ssline.sell.supermarket.model.Sucursal;
import com.ssline.sell.supermarket.repository.SucursalRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {

    @Mock
    private SucursalRepository repo;

    @InjectMocks
    private SucursalService sucursalService;

    private Sucursal sucursal;
    private SucursalDTO sucursalDTO;

    @BeforeEach
    void setUp() {
        sucursal = Sucursal.builder().id(1L).nombre("Centro").direccion("Av. Principal 123").build();
        sucursalDTO = SucursalDTO.builder().id(1L).nombre("Centro").direccion("Av. Principal 123").build();
    }

    // ── getAllSucursales ───────────────────────────────────────────────────────

    @Test
    void getAllSucursales_lista_vacia_retorna_lista_vacia() {
        when(repo.findAll()).thenReturn(List.of());

        List<SucursalDTO> result = sucursalService.getAllSucursales();

        assertThat(result).isEmpty();
        verify(repo).findAll();
    }

    @Test
    void getAllSucursales_retorna_lista_mapeada() {
        when(repo.findAll()).thenReturn(List.of(sucursal));

        List<SucursalDTO> result = sucursalService.getAllSucursales();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getNombre()).isEqualTo("Centro");
        assertThat(result.get(0).getDireccion()).isEqualTo("Av. Principal 123");
    }

    @Test
    void getAllSucursales_multiples_sucursales_retorna_todas() {
        Sucursal otra = Sucursal.builder().id(2L).nombre("Norte").direccion("Calle 45").build();
        when(repo.findAll()).thenReturn(List.of(sucursal, otra));

        List<SucursalDTO> result = sucursalService.getAllSucursales();

        assertThat(result).hasSize(2);
    }

    // ── createSucursal ────────────────────────────────────────────────────────

    @Test
    void createSucursal_guarda_y_retorna_dto_mapeado() {
        when(repo.save(any(Sucursal.class))).thenReturn(sucursal);

        SucursalDTO result = sucursalService.createSucursal(sucursalDTO);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Centro");
        assertThat(result.getDireccion()).isEqualTo("Av. Principal 123");
        verify(repo).save(any(Sucursal.class));
    }

    @Test
    void createSucursal_construye_entidad_sin_id() {
        when(repo.save(any(Sucursal.class))).thenReturn(sucursal);

        sucursalService.createSucursal(sucursalDTO);

        verify(repo).save(argThat(s -> s.getId() == null));
    }

    // ── updateSucursal ────────────────────────────────────────────────────────

    @Test
    void updateSucursal_no_encontrada_lanza_NotFoundException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sucursalService.updateSucursal(99L, sucursalDTO))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Sucursal no encontrada");

        verify(repo, never()).save(any());
    }

    @Test
    void updateSucursal_encontrada_actualiza_nombre_y_direccion() {
        SucursalDTO datosNuevos = SucursalDTO.builder().nombre("Norte").direccion("Calle 99").build();
        Sucursal actualizada = Sucursal.builder().id(1L).nombre("Norte").direccion("Calle 99").build();

        when(repo.findById(1L)).thenReturn(Optional.of(sucursal));
        when(repo.save(any(Sucursal.class))).thenReturn(actualizada);

        SucursalDTO result = sucursalService.updateSucursal(1L, datosNuevos);

        assertThat(result.getNombre()).isEqualTo("Norte");
        assertThat(result.getDireccion()).isEqualTo("Calle 99");
    }

    @Test
    void updateSucursal_modifica_entidad_antes_de_guardar() {
        SucursalDTO datosNuevos = SucursalDTO.builder().nombre("Sur").direccion("Av. Sur 5").build();

        when(repo.findById(1L)).thenReturn(Optional.of(sucursal));
        when(repo.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        sucursalService.updateSucursal(1L, datosNuevos);

        verify(repo).save(argThat(s ->
                s.getNombre().equals("Sur") && s.getDireccion().equals("Av. Sur 5")
        ));
    }

    // ── eliminarSucursal ──────────────────────────────────────────────────────

    @Test
    void eliminarSucursal_no_existe_lanza_NotFoundException() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> sucursalService.eliminarSucursal(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Sucursal no encontrada");

        verify(repo, never()).deleteById(any());
    }

    @Test
    void eliminarSucursal_existe_invoca_deleteById() {
        when(repo.existsById(1L)).thenReturn(true);

        sucursalService.eliminarSucursal(1L);

        verify(repo).deleteById(1L);
    }
}
