package com.ssline.sell.supermarket.integration;

import com.ssline.sell.supermarket.dto.ProductoDTO;
import com.ssline.sell.supermarket.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductoIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductoRepository productoRepository;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/productos";
    }

    @BeforeEach
    void limpiarDB() {
        productoRepository.deleteAll();
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Test
    void GET_lista_vacia_retorna_200_y_array_vacio() {
        ResponseEntity<ProductoDTO[]> response =
                restTemplate.getForEntity(baseUrl(), ProductoDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void GET_retorna_productos_existentes() {
        ProductoDTO nuevo = ProductoDTO.builder()
                .nombre("Leche").categoria("Lacteos").precio(1500.0).cantidad(10).build();
        restTemplate.postForEntity(baseUrl(), nuevo, ProductoDTO.class);

        ResponseEntity<ProductoDTO[]> response =
                restTemplate.getForEntity(baseUrl(), ProductoDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getNombre()).isEqualTo("Leche");
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Test
    void POST_crea_producto_y_retorna_201() {
        ProductoDTO nuevo = ProductoDTO.builder()
                .nombre("Pan").categoria("Panaderia").precio(300.0).cantidad(20).build();

        ResponseEntity<ProductoDTO> response =
                restTemplate.postForEntity(baseUrl(), nuevo, ProductoDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getNombre()).isEqualTo("Pan");
        assertThat(response.getBody().getCategoria()).isEqualTo("Panaderia");
        assertThat(response.getBody().getPrecio()).isEqualTo(300.0);
        assertThat(response.getBody().getCantidad()).isEqualTo(20);
    }

    @Test
    void POST_persiste_en_base_de_datos() {
        ProductoDTO nuevo = ProductoDTO.builder()
                .nombre("Yogur").categoria("Lacteos").precio(800.0).cantidad(5).build();
        restTemplate.postForEntity(baseUrl(), nuevo, ProductoDTO.class);

        assertThat(productoRepository.count()).isEqualTo(1);
        assertThat(productoRepository.findByNombre("Yogur")).isPresent();
    }

    // ── PUT ───────────────────────────────────────────────────────────────────

    @Test
    void PUT_actualiza_producto_y_retorna_200() {
        ProductoDTO creado = restTemplate
                .postForEntity(baseUrl(), ProductoDTO.builder()
                        .nombre("Queso").categoria("Lacteos").precio(2000.0).cantidad(3).build(),
                        ProductoDTO.class)
                .getBody();

        assertThat(creado).isNotNull();

        ProductoDTO actualizado = ProductoDTO.builder()
                .nombre("Queso Cremoso").categoria("Lacteos Premium").precio(2500.0).cantidad(2).build();

        HttpEntity<ProductoDTO> request = new HttpEntity<>(actualizado);
        ResponseEntity<ProductoDTO> response = restTemplate.exchange(
                baseUrl() + "/" + creado.getId(), HttpMethod.PUT, request, ProductoDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNombre()).isEqualTo("Queso Cremoso");
        assertThat(response.getBody().getPrecio()).isEqualTo(2500.0);
    }

    @Test
    void PUT_id_inexistente_retorna_404() {
        HttpEntity<ProductoDTO> request = new HttpEntity<>(
                ProductoDTO.builder().nombre("X").categoria("Y").precio(1.0).cantidad(1).build());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/9999", HttpMethod.PUT, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test
    void DELETE_elimina_producto_y_retorna_204() {
        ProductoDTO creado = restTemplate
                .postForEntity(baseUrl(), ProductoDTO.builder()
                        .nombre("Manteca").categoria("Lacteos").precio(500.0).cantidad(8).build(),
                        ProductoDTO.class)
                .getBody();

        assertThat(creado).isNotNull();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + creado.getId(), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(productoRepository.count()).isEqualTo(0);
    }

    @Test
    void DELETE_id_inexistente_retorna_404() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/9999", HttpMethod.DELETE, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
