Feature: Gestión de Sucursales
  Como operador del supermercado
  Quiero poder crear, consultar, actualizar y eliminar sucursales
  Para mantener la red de puntos de venta actualizada

  Background:
    * url baseUrl

  Scenario: Listar sucursales retorna 200 y un array
    Given path '/api/sucursales'
    When method GET
    Then status 200
    And match response == '#[]'

  Scenario: Crear una sucursal retorna 201 con el recurso creado
    Given path '/api/sucursales'
    And request
      """
      {
        "nombre": "Sucursal Sur",
        "direccion": "Av. del Sur 789"
      }
      """
    When method POST
    Then status 201
    And match response.id == '#notnull'
    And match response.nombre == 'Sucursal Sur'
    And match response.direccion == 'Av. del Sur 789'

  Scenario: Crear una sucursal y actualizarla
    # Crear
    Given path '/api/sucursales'
    And request { nombre: 'Sucursal Vieja', direccion: 'Dir. Vieja 1' }
    When method POST
    Then status 201
    * def sid = response.id

    # Actualizar
    Given path '/api/sucursales/' + sid
    And request { nombre: 'Sucursal Renovada', direccion: 'Dir. Nueva 99' }
    When method PUT
    Then status 200
    And match response.id == sid
    And match response.nombre == 'Sucursal Renovada'
    And match response.direccion == 'Dir. Nueva 99'

  Scenario: Crear una sucursal y eliminarla
    # Crear
    Given path '/api/sucursales'
    And request { nombre: 'Sucursal Temporal', direccion: 'Dir. Temp' }
    When method POST
    Then status 201
    * def sid = response.id

    # Eliminar
    Given path '/api/sucursales/' + sid
    When method DELETE
    Then status 204

  Scenario: Actualizar una sucursal inexistente retorna 404
    Given path '/api/sucursales/999999'
    And request { nombre: 'X', direccion: 'Y' }
    When method PUT
    Then status 404

  Scenario: Eliminar una sucursal inexistente retorna 404
    Given path '/api/sucursales/999999'
    When method DELETE
    Then status 404
