package com.ssline.sell.supermarket.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetalleVentaDTO {
    private Long id;
    private String nombreProducto;
    private Integer cantProd;
    private Double precio;
    private Double subtotal;
}
