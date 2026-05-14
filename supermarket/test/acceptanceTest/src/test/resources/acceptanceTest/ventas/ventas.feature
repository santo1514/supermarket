Feature: Gestión de Ventas
  Como operador del supermercado
  Quiero poder registrar, consultar, actualizar y cancelar ventas
  Para gestionar las transacciones del negocio correctamente

  Background:
    * url baseUrl

    # Nombres únicos por escenario para evitar colisión entre runs paralelos o repetidos
    * def ts = java.lang.System.currentTimeMillis()
    * def productoNombreUnico = 'Arroz-' + ts
    * def sucursalNombreUnica = 'Suc-Ventas-' + ts

    Given path '/api/productos'
    And request { nombre: '#(productoNombreUnico)', categoria: 'Almacen', precio: 400.0, cantidad: 200 }
    When method POST
    Then status 201
    * def productoNombre = response.nombre
    * def productoPrecio = response.precio

    Given path '/api/sucursales'
    And request { nombre: '#(sucursalNombreUnica)', direccion: 'Calle Test 1' }
    When method POST
    Then status 201
    * def sucursalId = response.id

  Scenario: Listar ventas retorna 200 y un array
    Given path '/api/ventas'
    When method GET
    Then status 200
    And match response == '#[]'

  Scenario: Crear una venta retorna 201 con el recurso creado y total calculado
    Given path '/api/ventas'
    And request
      """
      {
        "fecha": "2024-08-15",
        "estado": "PENDIENTE",
        "sucursalId": #(sucursalId),
        "total": 800.0,
        "detalle": [
          {
            "nombreProducto": "#(productoNombre)",
            "cantProd": 2,
            "precio": #(productoPrecio),
            "subtotal": 800.0
          }
        ]
      }
      """
    When method POST
    Then status 201
    And match response.id == '#notnull'
    And match response.estado == 'PENDIENTE'
    And match response.sucursalId == sucursalId
    And match response.total == 800.0
    And match response.detalle == '#[1]'
    And match response.detalle[0].nombreProducto == '#(productoNombre)'
    And match response.detalle[0].cantProd == 2
    And match response.detalle[0].subtotal == 800.0

  Scenario: Actualizar el estado de una venta existente
    Given path '/api/ventas'
    And request
      """
      {
        "fecha": "2024-09-01",
        "estado": "PENDIENTE",
        "sucursalId": #(sucursalId),
        "total": 400.0,
        "detalle": [
          {
            "nombreProducto": "#(productoNombre)",
            "cantProd": 1,
            "precio": #(productoPrecio),
            "subtotal": 400.0
          }
        ]
      }
      """
    When method POST
    Then status 201
    * def ventaId = response.id

    Given path '/api/ventas/' + ventaId
    And request { "estado": "COMPLETADO" }
    When method PUT
    Then status 200
    And match response.id == ventaId
    And match response.estado == 'COMPLETADO'

  Scenario: Eliminar una venta existente retorna 204
    Given path '/api/ventas'
    And request
      """
      {
        "fecha": "2024-10-01",
        "estado": "PENDIENTE",
        "sucursalId": #(sucursalId),
        "total": 400.0,
        "detalle": [
          {
            "nombreProducto": "#(productoNombre)",
            "cantProd": 1,
            "precio": #(productoPrecio),
            "subtotal": 400.0
          }
        ]
      }
      """
    When method POST
    Then status 201
    * def ventaId = response.id

    Given path '/api/ventas/' + ventaId
    When method DELETE
    Then status 204

  Scenario: Crear venta con sucursal inexistente retorna 404
    Given path '/api/ventas'
    And request
      """
      {
        "fecha": "2024-11-01",
        "estado": "PENDIENTE",
        "sucursalId": 999999,
        "total": 400.0,
        "detalle": [
          {
            "nombreProducto": "#(productoNombre)",
            "cantProd": 1,
            "precio": #(productoPrecio),
            "subtotal": 400.0
          }
        ]
      }
      """
    When method POST
    Then status 404

  Scenario: Crear venta con producto inexistente retorna 500
    Given path '/api/ventas'
    And request
      """
      {
        "fecha": "2024-11-01",
        "estado": "PENDIENTE",
        "sucursalId": #(sucursalId),
        "total": 100.0,
        "detalle": [
          {
            "nombreProducto": "ProductoQueNoExiste",
            "cantProd": 1,
            "precio": 100.0,
            "subtotal": 100.0
          }
        ]
      }
      """
    When method POST
    Then status 500

  Scenario: Crear venta sin detalle retorna 500
    Given path '/api/ventas'
    And request
      """
      {
        "fecha": "2024-11-01",
        "estado": "PENDIENTE",
        "sucursalId": #(sucursalId),
        "total": 0.0,
        "detalle": []
      }
      """
    When method POST
    Then status 500

  Scenario: Actualizar una venta inexistente retorna 500
    Given path '/api/ventas/999999'
    And request { "estado": "COMPLETADO" }
    When method PUT
    Then status 500

  Scenario: Eliminar una venta inexistente retorna 500
    Given path '/api/ventas/999999'
    When method DELETE
    Then status 500
