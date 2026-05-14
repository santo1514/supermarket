package com.ssline.sell.supermarket.integration;

import com.ssline.sell.supermarket.dto.SucursalDTO;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SucursalIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/sucursales";
    }

    @BeforeEach
    void limpiarDB() {
        ventaRepository.deleteAll();
        sucursalRepository.deleteAll();
    }

    // ── GET ───────────────────────────────────────────────────────────────────

    @Test
    void GET_lista_vacia_retorna_200_y_array_vacio() {
        ResponseEntity<SucursalDTO[]> response =
                restTemplate.getForEntity(baseUrl(), SucursalDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void GET_retorna_sucursales_existentes() {
        restTemplate.postForEntity(baseUrl(),
                SucursalDTO.builder().nombre("Centro").direccion("Av. Principal 1").build(),
                SucursalDTO.class);

        ResponseEntity<SucursalDTO[]> response =
                restTemplate.getForEntity(baseUrl(), SucursalDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getNombre()).isEqualTo("Centro");
    }

    // ── POST ──────────────────────────────────────────────────────────────────

    @Test
    void POST_crea_sucursal_y_retorna_201() {
        SucursalDTO nueva = SucursalDTO.builder()
                .nombre("Norte").direccion("Calle 45 Norte").build();

        ResponseEntity<SucursalDTO> response =
                restTemplate.postForEntity(baseUrl(), nueva, SucursalDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getNombre()).isEqualTo("Norte");
        assertThat(response.getBody().getDireccion()).isEqualTo("Calle 45 Norte");
    }

    @Test
    void POST_incluye_header_location_con_id() {
        SucursalDTO nueva = SucursalDTO.builder()
                .nombre("Sur").direccion("Av. Sur 99").build();

        ResponseEntity<SucursalDTO> response =
                restTemplate.postForEntity(baseUrl(), nueva, SucursalDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getHeaders().getLocation().toString())
                .startsWith("/api/sucursales/");
    }

    @Test
    void POST_persiste_en_base_de_datos() {
        restTemplate.postForEntity(baseUrl(),
                SucursalDTO.builder().nombre("Este").direccion("Calle Este 12").build(),
                SucursalDTO.class);

        assertThat(sucursalRepository.count()).isEqualTo(1);
    }

    // ── PUT ───────────────────────────────────────────────────────────────────

    @Test
    void PUT_actualiza_sucursal_y_retorna_200() {
        SucursalDTO creada = restTemplate.postForEntity(baseUrl(),
                SucursalDTO.builder().nombre("Vieja").direccion("Dir. Vieja").build(),
                SucursalDTO.class).getBody();

        assertThat(creada).isNotNull();

        SucursalDTO actualizada = SucursalDTO.builder()
                .nombre("Nueva").direccion("Dir. Nueva").build();

        HttpEntity<SucursalDTO> request = new HttpEntity<>(actualizada);
        ResponseEntity<SucursalDTO> response = restTemplate.exchange(
                baseUrl() + "/" + creada.getId(), HttpMethod.PUT, request, SucursalDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getNombre()).isEqualTo("Nueva");
        assertThat(response.getBody().getDireccion()).isEqualTo("Dir. Nueva");
    }

    @Test
    void PUT_id_inexistente_retorna_404() {
        HttpEntity<SucursalDTO> request = new HttpEntity<>(
                SucursalDTO.builder().nombre("X").direccion("Y").build());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/9999", HttpMethod.PUT, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test
    void DELETE_elimina_sucursal_y_retorna_204() {
        SucursalDTO creada = restTemplate.postForEntity(baseUrl(),
                SucursalDTO.builder().nombre("Temp").direccion("Dir Temp").build(),
                SucursalDTO.class).getBody();

        assertThat(creada).isNotNull();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + creada.getId(), HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(sucursalRepository.count()).isEqualTo(0);
    }

    @Test
    void DELETE_id_inexistente_retorna_404() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/9999", HttpMethod.DELETE, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
