package com.ssline.sell.supermarket.services;

import java.util.List;
import com.ssline.sell.supermarket.dto.VentaDTO;


public interface IVentaService {
    List<VentaDTO> traerVentas();
    VentaDTO crearVenta(VentaDTO ventaDto);
    VentaDTO actualizarVenta(Long id, VentaDTO ventaDto);
    void eliminarVenta(Long id);
}
