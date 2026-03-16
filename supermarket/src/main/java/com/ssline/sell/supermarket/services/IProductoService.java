package com.ssline.sell.supermarket.services;

import java.util.List;

import com.ssline.sell.supermarket.dto.ProductoDTO;

public interface IProductoService {
    List<ProductoDTO> traerProductos();
    ProductoDTO crearProducto(ProductoDTO productoDto);
    ProductoDTO actualizarProducto(Long id, ProductoDTO productoDto);
    void eliminarProducto(Long id);
}
