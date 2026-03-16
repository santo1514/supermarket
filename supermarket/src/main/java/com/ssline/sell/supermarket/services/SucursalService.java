package com.ssline.sell.supermarket.services;

import com.ssline.sell.supermarket.dto.SucursalDTO;
import com.ssline.sell.supermarket.exception.NotFoundException;
import com.ssline.sell.supermarket.mapper.Mapper;
import com.ssline.sell.supermarket.model.Sucursal;
import com.ssline.sell.supermarket.repository.SucursalRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("null")
public class SucursalService implements ISucursalService {

    @Autowired
    private SucursalRepository repo;

    @Override
    public List<SucursalDTO> getAllSucursales() {
        return repo.findAll()
                .stream()
                .map(Mapper::toDTO)
                .toList();
    }
    
    @Override
    public SucursalDTO createSucursal(SucursalDTO sucursalDto) {
        Sucursal suc = Sucursal.builder()
                .nombre(sucursalDto.getNombre())
                .direccion(sucursalDto.getDireccion())
                .build();

        return Mapper.toDTO(repo.save(suc));
    }

    @Override
    public SucursalDTO updateSucursal(Long id, SucursalDTO sucursalDto) {
        Sucursal suc = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        suc.setNombre(sucursalDto.getNombre());
        suc.setDireccion(sucursalDto.getDireccion());

        return Mapper.toDTO(repo.save(suc));
    }

    @Override
    public void eliminarSucursal(Long id) {
        if (!repo.existsById(id))
            throw new NotFoundException("Sucursal no encontrada");

        repo.deleteById(id);
    }
}
