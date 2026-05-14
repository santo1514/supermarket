package com.ssline.sell.supermarket.integration;

import com.ssline.sell.supermarket.dto.DetalleVentaDTO;
import com.ssline.sell.supermarket.dto.ProductoDTO;
import com.ssline.sell.supermarket.dto.SucursalDTO;
import com.ssline.sell.supermarket.dto.VentaDTO;
import com.ssline.sell.supermarket.repository.ProductoRepository;
import com.ssline.sell.supermarket.repository.SucursalRepository;
import com.ssline.sell.supermarket.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VentaIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    private Long sucursalId;
    private String productoNombre;

    private String ventasUrl() {
        return "http://localhost:" + port + "/api/ventas";
    }

    private String productosUrl() {
        return "http://localhost:" + port + "/api/productos";
    }

    private String sucursalesUrl() {
        return "http://localhost:" + port + "/api/sucursales";
    }

    @BeforeEach
    void configurarDatos() {
        ventaRepository.deleteAll();
        productoRepository.deleteAll();
        sucursalRepository.deleteAll();

        SucursalDTO suc = restTemplate.postForEntity(sucursalesUrl(),
                SucursalDTO.builder().nombre("Centro").direccion("Av. 1").build(),
                SucursalDTO.class).getBody();
        assertThat(suc).isNotNull();
        sucursalId = suc.getId();

        productoNombre = "Pan";
        restTemplate.postForEntity(productosUrl(),
                ProductoDTO.builder().nombre(productoNombre).categoria("Panaderia")
                        .precio(500.0).cantidad(50).build(),
                ProductoDTO.class);
    }

    private VentaDTO ventaDTOValida() {
        DetalleVentaDTO detalle = DetalleVentaDTO.builder()
                .nombreProducto(productoNombre).cantProd(2).precio(500.0).subtotal(1000.0).build();
        return VentaDTO.builder()
                .fecha(LocalDate.of(2024, 6, 1))
                .estado("PENDIENTE")
                .sucursalId(sucursalId)
                .total(1000.0)
                .detalle(List.of(detalle))
                .build();
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Test
    void GET_lista_vacia_retorna_200_y_array_vacio() {
        ResponseEntity<VentaDTO[]> response =
                restTemplate.getForEntity(ventasUrl(), VentaDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void GET_retorna_ventas_existentes() {
        restTemplate.postForEntity(ventasUrl(), ventaDTOValida(), VentaDTO.class);

        ResponseEntity<VentaDTO[]> response =
                restTemplate.getForEntity(ventasUrl(), VentaDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Test
    void POST_crea_venta_y_retorna_201() {
        ResponseEntity<VentaDTO> response =
                restTemplate.postForEntity(ventasUrl(), ventaDTOValida(), VentaDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getEstado()).isEqualTo("PENDIENTE");
        assertThat(response.getBody().getSucursalId()).isEqualTo(sucursalId);
        assertThat(response.getBody().getDetalle()).hasSize(1);
    }

    @Test
    void POST_calcula_total_correctamente() {
        ResponseEntity<VentaDTO> response =
                restTemplate.postForEntity(ventasUrl(), ventaDTOValida(), VentaDTO.class);

        assertThat(response.getBody()).isNotNull();
        // 500 * 2 = 1000
        assertThat(response.getBody().getTotal()).isEqualTo(1000.0);
    }

    @Test
    void POST_incluye_header_location() {
        ResponseEntity<VentaDTO> response =
                restTemplate.postForEntity(ventasUrl(), ventaDTOValida(), VentaDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
    }

    @Test
    void POST_sucursal_inexistente_retorna_404() {
        VentaDTO dto = ventaDTOValida();
        dto.setSucursalId(9999L);

        ResponseEntity<String> response =
                restTemplate.postForEntity(ventasUrl(), dto, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void POST_producto_inexistente_retorna_500() {
        DetalleVentaDTO detalleInvalido = DetalleVentaDTO.builder()
                .nombreProducto("ProductoQueNoExiste").cantProd(1).precio(100.0).subtotal(100.0).build();
        VentaDTO dto = VentaDTO.builder()
                .fecha(LocalDate.now())
                .estado("PENDIENTE")
                .sucursalId(sucursalId)
                .total(100.0)
                .detalle(List.of(detalleInvalido))
                .build();

        ResponseEntity<String> response =
                restTemplate.postForEntity(ventasUrl(), dto, String.class);

        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    void POST_sin_detalle_retorna_500() {
        VentaDTO dto = VentaDTO.builder()
                .fecha(LocalDate.now()).estado("PENDIENTE")
                .sucursalId(sucursalId).total(0.0).detalle(List.of()).build();

        ResponseEntity<String> response =
                restTemplate.postForEntity(ventasUrl(), dto, String.class);

        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
    }

    // ── PUT ───────────────────────────────────────────────────────────────────

    @Test
    void PUT_actualiza_estado_y_retorna_200() {
        VentaDTO creada = restTemplate
                .postForEntity(ventasUrl(), ventaDTOValida(), VentaDTO.class).getBody();

        assertThat(creada).isNotNull();

        VentaDTO actualizacion = VentaDTO.builder().estado("COMPLETADO").build();
        HttpEntity<VentaDTO> request = new HttpEntity<>(actualizacion);

        ResponseEntity<VentaDTO> response = restTemplate.exchange(
                ventasUrl() + "/" + creada.getId(), HttpMethod.PUT, request, VentaDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEstado()).isEqualTo("COMPLETADO");
    }

    @Test
    void PUT_id_inexistente_retorna_500() {
        HttpEntity<VentaDTO> request = new HttpEntity<>(
                VentaDTO.builder().estado("CANCELADO").build());

        ResponseEntity<String> response = restTemplate.exchange(
                ventasUrl() + "/9999", HttpMethod.PUT, request, String.class);

        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test
    void DELETE_elimina_venta_y_retorna_204() {
        VentaDTO creada = restTemplate
                .postForEntity(ventasUrl(), ventaDTOValida(), VentaDTO.class).getBody();

        assertThat(creada).isNotNull();

        ResponseEntity<Void> response = restTemplate.exchange(
                ventasUrl() + "/" + creada.getId(), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(ventaRepository.count()).isEqualTo(0);
    }

    @Test
    void DELETE_id_inexistente_retorna_500() {
        ResponseEntity<String> response = restTemplate.exchange(
                ventasUrl() + "/9999", HttpMethod.DELETE, null, String.class);

        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
    }
}
