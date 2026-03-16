package com.ssline.sell.supermarket.services;

import java.util.List;

import com.ssline.sell.supermarket.dto.SucursalDTO;

public interface ISucursalService {
    
    List<SucursalDTO> getAllSucursales();
    SucursalDTO createSucursal(SucursalDTO sucursalDTO);
    SucursalDTO updateSucursal(Long id, SucursalDTO sucursalDTO);
    void eliminarSucursal(Long id);
}