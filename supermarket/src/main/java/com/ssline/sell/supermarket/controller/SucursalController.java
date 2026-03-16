package com.ssline.sell.supermarket.controller;

import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ssline.sell.supermarket.dto.SucursalDTO;
import com.ssline.sell.supermarket.services.ISucursalService;

@RestController
@RequestMapping("/api/sucursales")
@SuppressWarnings("null")
public class SucursalController {
    @Autowired
    private ISucursalService sucursalService;

    @GetMapping
    public ResponseEntity<List<SucursalDTO>> traerSucursales() {
        return ResponseEntity.ok(sucursalService.getAllSucursales());
    }

    @PostMapping
    public ResponseEntity<SucursalDTO> create(@RequestBody SucursalDTO dto) {
        SucursalDTO created = sucursalService.createSucursal(dto);
        return ResponseEntity.created(URI.create("/api/sucursales/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SucursalDTO> update(@PathVariable Long id, @RequestBody SucursalDTO dto) {
        return ResponseEntity.ok(sucursalService.updateSucursal(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sucursalService.eliminarSucursal(id);
        return ResponseEntity.noContent().build();
    }
}