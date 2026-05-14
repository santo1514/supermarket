Feature: Gestión de Productos
  Como operador del supermercado
  Quiero poder crear, consultar, actualizar y eliminar productos
  Para mantener el catálogo actualizado

  Background:
    * url baseUrl

  Scenario: Listar productos retorna 200 y un array
    Given path '/api/productos'
    When method GET
    Then status 200
    And match response == '#[]'

  Scenario: Crear un producto retorna 201 con el recurso creado
    Given path '/api/productos'
    And request
      """
      {
        "nombre": "Yogur Natural",
        "categoria": "Lacteos",
        "precio": 950.0,
        "cantidad": 40
      }
      """
    When method POST
    Then status 201
    And match response.id == '#notnull'
    And match response.nombre == 'Yogur Natural'
    And match response.categoria == 'Lacteos'
    And match response.precio == 950.0
    And match response.cantidad == 40

  Scenario: Crear un producto y actualizarlo
    # Crear
    Given path '/api/productos'
    And request { nombre: 'Manteca', categoria: 'Lacteos', precio: 500.0, cantidad: 20 }
    When method POST
    Then status 201
    * def pid = response.id

    # Actualizar
    Given path '/api/productos/' + pid
    And request { nombre: 'Manteca Premium', categoria: 'Lacteos', precio: 750.0, cantidad: 15 }
    When method PUT
    Then status 200
    And match response.id == pid
    And match response.nombre == 'Manteca Premium'
    And match response.precio == 750.0
    And match response.cantidad == 15

  Scenario: Crear un producto y eliminarlo
    # Crear
    Given path '/api/productos'
    And request { nombre: 'Producto Temporal', categoria: 'Test', precio: 1.0, cantidad: 1 }
    When method POST
    Then status 201
    * def pid = response.id

    # Eliminar
    Given path '/api/productos/' + pid
    When method DELETE
    Then status 204

  Scenario: Actualizar un producto inexistente retorna 404
    Given path '/api/productos/999999'
    And request { nombre: 'X', categoria: 'Y', precio: 1.0, cantidad: 1 }
    When method PUT
    Then status 404

  Scenario: Eliminar un producto inexistente retorna 404
    Given path '/api/productos/999999'
    When method DELETE
    Then status 404
