# PuntoVenta

Aplicacion movil de Punto de venta diseñada para administrar ventas, inventario, clientes con control de creditos, pedidos, proveedores y finanzas en tiempo real desde uno o multiples negocios.

------------

## Modulos principales 

* **Gestión Multi-Negocio:**
  * **Negocio Principal:** Configuración y operación central del comercio activo.
  * **Agregar Otro Negocio:** Soporte para registrar y alternar entre múltiples sucursales o negocios independientes.
* **Ventas:**
  * Registro rápido de ventas filtrado por **productos** y **categorías**.
* **Clientes y Control de Créditos:**
  * Modalidades de pago: **Contado**, **Transferencia** y **Crédito**.
  * Gestión de mercancía a crédito con plazos definidos (**15 días** o **1 mes**).
  * Cálculo automático de **total por cliente** y **total general por cobrar**.
* **Inventario:**
  * Registro completo por producto: nombre, categoría, precio de venta, precio de costo, unidades disponibles, código de barras, imagen y descripción.
  * Soporte de **variantes opcionales**: colores, tallas, marca, sabor y material.
* **Pedidos:**
  * Seguimiento de clientes con pedidos pendientes o apartados de productos.
* **Proveedores:**
  * Catálogo de proveedores y vinculación directa de cada producto con su proveedor correspondiente.
* **Estadísticas y Control Financiero:**
  * Reporte de **Ingresos** y **Egresos** (gastos operativos del negocio).
  * **Balance general** desglosado entre **Efectivo** y **Cuenta Bancaria**.

 ----------

 ## Requisitos Previos

* SDK: 37.2 / Flutter: 3.50.0
* **Plataforma destino: Android 10 - 17
* **Herramientas de compilación: Gradle (Android)

---------

## Reglas

* ** No subir nada en la rama main hasta que el programa sea estable y listo para maquetar
* ** Usar su rama asignada *dev: feat/frontend; feat/backend* donde se probaran los modulos antes de pasar a main, enfocados en cada area
* ** Ordenar sus propias carpetas dependiendo el area de desarrollo para mejor organizacion.
* ** La carpeta de *docs* es para una documentacion compartida o notas tecnicas.

-------
## Reglas de seguridad

* **Cero secretos en git: Jamas subir claves API, tokens, credenciales de base de datos ni contraseñas
