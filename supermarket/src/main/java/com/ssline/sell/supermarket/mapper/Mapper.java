package com.ssline.sell.supermarket.mapper;

import java.util.stream.Collectors;

import com.ssline.sell.supermarket.dto.DetalleVentaDTO;
import com.ssline.sell.supermarket.dto.ProductoDTO;
import com.ssline.sell.supermarket.dto.SucursalDTO;
import com.ssline.sell.supermarket.dto.VentaDTO;
import com.ssline.sell.supermarket.model.Producto;
import com.ssline.sell.supermarket.model.Sucursal;
import com.ssline.sell.supermarket.model.Venta;

 @SuppressWarnings("null")
public class Mapper {

    private Mapper() {}

    // Mapeo de Producto a ProductoDTO
    public static ProductoDTO toDTO(Producto p) {
        if (p == null) return null;
        
        return ProductoDTO.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .categoria(p.getCategoria())
                .precio(p.getPrecio())
                .cantidad(p.getCantidad())
                .build();
    }

    // Mapeo de Venta a VentaDTO
    public static VentaDTO toDTO(Venta v) {
        if (v == null) return null;

        var detalle = v.getDetalle().stream().map(det ->
                DetalleVentaDTO.builder()
                        .id(det.getProducto().getId())
                        .nombreProducto(det.getProducto().getNombre())
                        .cantProd(det.getCantProd())
                        .precio(det.getPrecio())
                        .subtotal(det.getPrecio() * det.getCantProd())
                        .build()
        ).collect(Collectors.toList());
       
        var total = detalle.stream()
                .map(DetalleVentaDTO::getSubtotal)
                .reduce(0.0, Double::sum);

       return VentaDTO.builder()
                .id(v.getId())
                .fecha(v.getFecha())
                .estado(v.getEstado())
                .sucursalId(v.getSucursal().getId())
                .detalle(detalle)
                .total(total)
                .build();
    }

    // Mapeo de Sucursal a SucursalDTO
    public static SucursalDTO toDTO(Sucursal s) {
        if (s == null) return null;

        return SucursalDTO.builder()
                .id(s.getId())
                .nombre(s.getNombre())
                .direccion(s.getDireccion())
                .build();
    }

    
}
