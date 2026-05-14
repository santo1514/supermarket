Feature: Flujo Completo de Venta
  Como operador del supermercado
  Quiero crear un producto, registrar una sucursal y realizar una venta
  Para confirmar que el ciclo completo de negocio funciona correctamente

  Background:
    * url baseUrl

  Scenario: Crear producto, registrar sucursal y realizar una venta exitosa

    # Nombres únicos para evitar colisiones entre ejecuciones
    * def ts = java.lang.System.currentTimeMillis()

    # ──────────────────────────────────────────────────────────────
    # PASO 1: Crear el producto que se venderá
    # ──────────────────────────────────────────────────────────────
    Given path '/api/productos'
    And request
      """
      {
        "nombre": "Leche-#(ts)",
        "categoria": "Lacteos",
        "precio": 1500.0,
        "cantidad": 100
      }
      """
    When method POST
    Then status 201
    And match response.id == '#notnull'
    And match response.categoria == 'Lacteos'
    And match response.precio == 1500.0
    And match response.cantidad == 100
    * def productoId = response.id
    * def productoNombre = response.nombre
    * def productoPrecio = response.precio

    # ──────────────────────────────────────────────────────────────
    # PASO 2: Verificar que el producto aparece en el listado
    # ──────────────────────────────────────────────────────────────
    Given path '/api/productos'
    When method GET
    Then status 200
    And match response == '#[]'
    And match response[*].id contains productoId

    # ──────────────────────────────────────────────────────────────
    # PASO 3: Crear la sucursal donde se realizará la venta
    # ──────────────────────────────────────────────────────────────
    Given path '/api/sucursales'
    And request
      """
      {
        "nombre": "Sucursal-#(ts)",
        "direccion": "Av. Principal 123"
      }
      """
    When method POST
    Then status 201
    And match response.id == '#notnull'
    And match response.direccion == 'Av. Principal 123'
    * def sucursalId = response.id

    # ──────────────────────────────────────────────────────────────
    # PASO 4: Verificar que la sucursal aparece en el listado
    # ──────────────────────────────────────────────────────────────
    Given path '/api/sucursales'
    When method GET
    Then status 200
    And match response == '#[]'
    And match response[*].id contains sucursalId

    # ──────────────────────────────────────────────────────────────
    # PASO 5: Registrar la venta — 2 unidades × $1500 = $3000
    # ──────────────────────────────────────────────────────────────
    Given path '/api/ventas'
    And request
      """
      {
        "fecha": "2024-06-01",
        "estado": "PENDIENTE",
        "sucursalId": #(sucursalId),
        "total": 3000.0,
        "detalle": [
          {
            "nombreProducto": "#(productoNombre)",
            "cantProd": 2,
            "precio": #(productoPrecio),
            "subtotal": 3000.0
          }
        ]
      }
      """
    When method POST
    Then status 201
    And match response.id == '#notnull'
    And match response.estado == 'PENDIENTE'
    And match response.sucursalId == sucursalId
    And match response.total == 3000.0
    And match response.detalle == '#[1]'
    And match response.detalle[0].nombreProducto == '#(productoNombre)'
    And match response.detalle[0].cantProd == 2
    And match response.detalle[0].precio == '#(productoPrecio)'
    And match response.detalle[0].subtotal == 3000.0
    * def ventaId = response.id

    # ──────────────────────────────────────────────────────────────
    # PASO 6: Verificar que la venta aparece en el listado
    # ──────────────────────────────────────────────────────────────
    Given path '/api/ventas'
    When method GET
    Then status 200
    And match response == '#[]'
    And match response[*].id contains ventaId

    # ──────────────────────────────────────────────────────────────
    # PASO 7: Actualizar el estado de la venta a COMPLETADO
    # ──────────────────────────────────────────────────────────────
    Given path '/api/ventas/' + ventaId
    And request { "estado": "COMPLETADO" }
    When method PUT
    Then status 200
    And match response.id == ventaId
    And match response.estado == 'COMPLETADO'
    And match response.sucursalId == sucursalId


  Scenario: Venta con múltiples productos distintos calcula el total correctamente

    * def ts = java.lang.System.currentTimeMillis()

    # Producto 1
    Given path '/api/productos'
    And request { nombre: 'Pan-#(ts)', categoria: 'Panaderia', precio: 300.0, cantidad: 50 }
    When method POST
    Then status 201
    * def prod1Nombre = response.nombre
    * def prod1Precio = response.precio

    # Producto 2
    Given path '/api/productos'
    And request { nombre: 'Jugo-#(ts)', categoria: 'Bebidas', precio: 800.0, cantidad: 30 }
    When method POST
    Then status 201
    * def prod2Nombre = response.nombre
    * def prod2Precio = response.precio

    # Sucursal
    Given path '/api/sucursales'
    And request { nombre: 'Suc-Multi-#(ts)', direccion: 'Calle 45 Norte' }
    When method POST
    Then status 201
    * def sucId = response.id

    # Venta: 3×Pan($300)=900 + 2×Jugo($800)=1600 → total=2500
    Given path '/api/ventas'
    And request
      """
      {
        "fecha": "2024-07-10",
        "estado": "PENDIENTE",
        "sucursalId": #(sucId),
        "total": 2500.0,
        "detalle": [
          {
            "nombreProducto": "#(prod1Nombre)",
            "cantProd": 3,
            "precio": #(prod1Precio),
            "subtotal": 900.0
          },
          {
            "nombreProducto": "#(prod2Nombre)",
            "cantProd": 2,
            "precio": #(prod2Precio),
            "subtotal": 1600.0
          }
        ]
      }
      """
    When method POST
    Then status 201
    And match response.detalle == '#[2]'
    And match response.total == 2500.0
