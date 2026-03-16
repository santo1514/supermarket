package com.ssline.sell.supermarket.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ssline.sell.supermarket.dto.ProductoDTO;
import com.ssline.sell.supermarket.exception.NotFoundException;
import com.ssline.sell.supermarket.mapper.Mapper;
import com.ssline.sell.supermarket.model.Producto;
import com.ssline.sell.supermarket.repository.ProductoRepository;

@Service
public class ProductoService implements IProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public List<ProductoDTO> traerProductos() {
        return productoRepository.findAll().stream()
                .map(Mapper::toDTO)
                .toList();
    }

    @SuppressWarnings("null")
    @Override
    public ProductoDTO crearProducto(ProductoDTO productoDto) {
        var producto = Producto.builder()
                .nombre(productoDto.getNombre())
                .categoria(productoDto.getCategoria())
                .precio(productoDto.getPrecio())
                .cantidad(productoDto.getCantidad())
                .build();
        return Mapper.toDTO(productoRepository.save(producto));
    }

    @SuppressWarnings("null")
    @Override
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDto) {
        Producto producto = productoRepository.findById(id).orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + id));
        producto.setNombre(productoDto.getNombre());
        producto.setCategoria(productoDto.getCategoria());
        producto.setPrecio(productoDto.getPrecio());
        producto.setCantidad(productoDto.getCantidad());
        return Mapper.toDTO(productoRepository.save(producto));
    }

    @SuppressWarnings("null")
    @Override
    public void eliminarProducto(Long id) {
        Producto producto = productoRepository.findById(id).orElseThrow(() -> new NotFoundException("Producto no encontrado con id: " + id));
        productoRepository.delete(producto);
    }
    
}
