package com.ssline.sell.supermarket.dto;

import java.time.LocalDate;
import java.util.List;
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
public class VentaDTO {
    private Long id;
    private LocalDate fecha;
    private String estado;
    private Long sucursalId;
    private List<DetalleVentaDTO> detalle;
    private Double total;
}
