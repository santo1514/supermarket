package com.ssline.sell.supermarket.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssline.sell.supermarket.dto.DetalleVentaDTO;
import com.ssline.sell.supermarket.dto.VentaDTO;
import com.ssline.sell.supermarket.exception.NotFoundException;
import com.ssline.sell.supermarket.mapper.Mapper;
import com.ssline.sell.supermarket.model.DetalleVenta;
import com.ssline.sell.supermarket.model.Producto;
import com.ssline.sell.supermarket.model.Sucursal;
import com.ssline.sell.supermarket.model.Venta;
import com.ssline.sell.supermarket.repository.ProductoRepository;
import com.ssline.sell.supermarket.repository.SucursalRepository;
import com.ssline.sell.supermarket.repository.VentaRepository;

@Service
@SuppressWarnings("null")
public class VentaService implements IVentaService {

    @Autowired
    private VentaRepository ventaRepo;
    @Autowired
    private ProductoRepository productoRepo;
    @Autowired
    private SucursalRepository sucursalRepo;

    @Override
    public List<VentaDTO> traerVentas() {
        List<Venta> ventas = ventaRepo.findAll();
        List<VentaDTO> ventasDto = new ArrayList<>();
        VentaDTO dto;
        for (Venta v : ventas) {
            dto = Mapper.toDTO(v);
            ventasDto.add (dto);
        }
        return ventasDto;
    }

    @Override
    public VentaDTO crearVenta(VentaDTO ventaDto) {

        //Validaciones
        if (ventaDto == null) throw new RuntimeException("VentaDTO es null");
        if (ventaDto.getSucursalId() == null) throw new RuntimeException("Debe indicar la sucursal");
        if (ventaDto.getDetalle() == null || ventaDto.getDetalle().isEmpty())  throw new RuntimeException("Debe incluir al menos un producto");

        //Buscar la sucursal
        Sucursal suc = sucursalRepo.findById(ventaDto.getSucursalId()).orElse(null);
        if (suc == null) {
            throw new NotFoundException("Sucursal no encontrada");
        }

        //Crear la venta
        Venta vent = new Venta();
        vent.setFecha(ventaDto.getFecha());
        vent.setEstado(ventaDto.getEstado());
        vent.setSucursal(suc);
        vent.setTotal(ventaDto.getTotal());

         // La lista de detalles
        // --> Acá están los productos
        List<DetalleVenta> detalles = new ArrayList<>();
        Double totalCalculado = 0.0;

        for (DetalleVentaDTO detDTO : ventaDto.getDetalle()) {
            // Buscar producto por id (tu detDTO usa id como id de producto)
            Producto p = productoRepo.findByNombre(detDTO.getNombreProducto()).orElse(null);
            if (p == null)
            {throw new RuntimeException("Producto no encontrado: " + detDTO.getNombreProducto());}

            //Crear detalle
            DetalleVenta detalleVent = new DetalleVenta();
            detalleVent.setProducto(p);
            detalleVent.setPrecio(detDTO.getPrecio());
            detalleVent.setCantProd(detDTO.getCantProd());
            detalleVent.setVenta(vent);

            detalles.add(detalleVent);
            totalCalculado = totalCalculado+(detDTO.getPrecio()*detDTO.getCantProd());

        }
        //Seteamos la lista de detalle Venta
        vent.setDetalle(detalles);

        //guardamos en la BD
        vent = ventaRepo.save(vent);

        //Mapeo de salida
        VentaDTO ventaSalida = Mapper.toDTO(vent);

        return ventaSalida;
    }

    @Override
    public VentaDTO actualizarVenta(Long id, VentaDTO ventaDto) {
        //buscar si la venta existe para actualizarla
        Venta v = ventaRepo.findById(id).orElse(null);
        if (v == null) throw new RuntimeException("Venta no encontrada");

        if (ventaDto.getFecha()!=null) {
            v.setFecha(ventaDto.getFecha());
        }
        if(ventaDto.getEstado()!=null) {
            v.setEstado(ventaDto.getEstado());
        }

        if (ventaDto.getTotal()!=null) {
            v.setTotal(ventaDto.getTotal());
        }

        if (ventaDto.getSucursalId()!=null) {
            Sucursal suc = sucursalRepo.findById(ventaDto.getSucursalId()).orElse(null);
            if (suc == null) throw new NotFoundException("Sucursal no encontrada");
            v.setSucursal(suc);
        }
        ventaRepo.save(v);

        VentaDTO ventaSalida = Mapper.toDTO(v);

        return ventaSalida;
    }

    @Override
    public void eliminarVenta(Long id) {

        Venta v = ventaRepo.findById(id).orElse(null);
        if (v == null) throw new RuntimeException("Venta no encontrada");
        ventaRepo.delete(v);

    }
    
}
