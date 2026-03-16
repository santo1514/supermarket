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

import com.ssline.sell.supermarket.dto.VentaDTO;
import com.ssline.sell.supermarket.services.IVentaService;

@RestController
@RequestMapping("/api/ventas")
@SuppressWarnings("null")
public class VentaController {
    
    @Autowired
    private IVentaService ventaService;

    @GetMapping
    public ResponseEntity<List<VentaDTO>> traerVentas() {
        return ResponseEntity.ok(ventaService.traerVentas());
    }

    /**
     * Crea una venta usando directamente VentaDTO en la request (opción simple, sin request separado).
     * Se espera que el DTO traiga la información
     *
     */
    @PostMapping
    public ResponseEntity<VentaDTO> create(@RequestBody VentaDTO dto) {
        VentaDTO created = ventaService.crearVenta(dto);
        return ResponseEntity.created(URI.create("/api/ventas/" + created.getId()))
                .body(created);
    }

    @PutMapping("/{id}")
    public VentaDTO actualizar(@PathVariable Long id, @RequestBody VentaDTO dto) {
        // Actualiza fecha, estado, idSucursal, total y reemplaza el detalle
        return ventaService.actualizarVenta(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ventaService.eliminarVenta(id);
        return ResponseEntity.noContent().build();
    }
}
