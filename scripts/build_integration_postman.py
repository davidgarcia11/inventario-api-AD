#!/usr/bin/env python3
"""
Genera la colección Postman de tests de integración para Productos.
Se ejecuta automáticamente desde la GitHub Action y también se puede
correr en local con Newman.

Por qué generamos el JSON en vez de escribirlo a mano:
- El JSON de Postman es verboso (~50 líneas por request); editarlo
  manualmente es propenso a errores.
- Si en el futuro queremos extender los tests al resto de entidades
  basta con duplicar la sección de Productos cambiando el path base.
"""

import json
from pathlib import Path

OUT = Path("Inventario API - Integration Tests.postman_collection.json")


# ---------- helpers ----------

def make_request(name, method, path, *, body=None, tests=None, description=""):
    """Construye un item Postman v2.1.0 con scripts de test opcionales."""
    parsed_path = path.split("?", 1)
    url_path = parsed_path[0]
    query = []
    if len(parsed_path) == 2:
        for kv in parsed_path[1].split("&"):
            key, value = kv.split("=", 1)
            query.append({"key": key, "value": value})

    req = {
        "name": name,
        "request": {
            "method": method,
            "header": [],
            "url": {
                "raw": "{{baseUrl}}" + path,
                "host": ["{{baseUrl}}"],
                "path": [p for p in url_path.split("/") if p],
            },
            "description": description,
        },
        "response": [],
    }
    if query:
        req["request"]["url"]["query"] = query
    if body is not None:
        req["request"]["header"] = [
            {"key": "Content-Type", "value": "application/json"}
        ]
        req["request"]["body"] = {"mode": "raw", "raw": body}
    if tests:
        req["event"] = [
            {
                "listen": "test",
                "script": {"type": "text/javascript", "exec": tests},
            }
        ]
    return req


# ---------- AUTH ----------

# Register puede dar 200 (BD vacía) o 400 (usuario ya existe). En ambos
# casos el siguiente request (Login) deja un token válido en authToken.
register_tests = [
    "pm.test('Register devuelve 200 o 400 si el usuario ya existe', function () {",
    "    pm.expect([200, 400]).to.include(pm.response.code);",
    "});",
    "if (pm.response.code === 200) {",
    "    const json = pm.response.json();",
    "    pm.test('El response del Register tiene token', function () {",
    "        pm.expect(json).to.have.property('token');",
    "        pm.expect(json).to.have.property('username');",
    "        pm.expect(json).to.have.property('expiresIn');",
    "    });",
    "    pm.collectionVariables.set('authToken', json.token);",
    "}",
]

login_tests = [
    "pm.test('Login devuelve 200', function () {",
    "    pm.response.to.have.status(200);",
    "});",
    "const json = pm.response.json();",
    "pm.test('El response del Login tiene token, username y expiresIn', function () {",
    "    pm.expect(json).to.have.property('token').that.is.a('string');",
    "    pm.expect(json).to.have.property('username').that.eql(pm.collectionVariables.get('authUsername'));",
    "    pm.expect(json).to.have.property('expiresIn').that.is.a('number');",
    "});",
    "// Guardamos el token para que el resto de requests lo usen vía auth Bearer",
    "pm.collectionVariables.set('authToken', json.token);",
]

auth_folder = {
    "name": "0. Auth",
    "description": "Registra y autentica al usuario de pruebas. El token queda en {{authToken}}.",
    "auth": {"type": "noauth"},
    "item": [
        make_request(
            "Register usuario de pruebas",
            "POST",
            "/api/auth/register",
            body='{\n  "username": "{{authUsername}}",\n  "password": "{{authPassword}}"\n}',
            tests=register_tests,
        ),
        make_request(
            "Login",
            "POST",
            "/api/auth/login",
            body='{\n  "username": "{{authUsername}}",\n  "password": "{{authPassword}}"\n}',
            tests=login_tests,
        ),
    ],
}


# ---------- PRODUCTOS - HAPPY PATH ----------

crear_producto_tests = [
    "pm.test('POST producto devuelve 201', function () {",
    "    pm.response.to.have.status(201);",
    "});",
    "const json = pm.response.json();",
    "pm.test('La respuesta tiene id, nombre y sku', function () {",
    "    pm.expect(json).to.have.property('id').that.is.a('number');",
    "    pm.expect(json).to.have.property('nombre');",
    "    pm.expect(json).to.have.property('sku');",
    "    pm.expect(json).to.have.property('precioVenta');",
    "    pm.expect(json).to.have.property('stockTotal');",
    "    pm.expect(json).to.have.property('activo').that.is.a('boolean');",
    "});",
    "pm.test('Los valores enviados coinciden con los guardados', function () {",
    "    pm.expect(json.nombre).to.eql('Producto Integration');",
    "    pm.expect(json.sku).to.eql('INT-001');",
    "    pm.expect(json.precioVenta).to.eql(1.5);",
    "    pm.expect(json.stockTotal).to.eql(100);",
    "    pm.expect(json.activo).to.eql(true);",
    "});",
    "// Guardamos el id para usarlo en GET/PUT/PATCH/DELETE",
    "pm.collectionVariables.set('productoId', json.id);",
]

listar_productos_tests = [
    "pm.test('GET listado devuelve 200', function () {",
    "    pm.response.to.have.status(200);",
    "});",
    "const json = pm.response.json();",
    "pm.test('La respuesta es un array no vacío', function () {",
    "    pm.expect(json).to.be.an('array');",
    "    pm.expect(json.length).to.be.greaterThan(0);",
    "});",
    "pm.test('Contiene el producto recién creado', function () {",
    "    const id = Number(pm.collectionVariables.get('productoId'));",
    "    const encontrado = json.find(p => p.id === id);",
    "    pm.expect(encontrado).to.not.be.undefined;",
    "    pm.expect(encontrado.sku).to.eql('INT-001');",
    "});",
]

get_por_id_tests = [
    "pm.test('GET por id devuelve 200', function () {",
    "    pm.response.to.have.status(200);",
    "});",
    "const json = pm.response.json();",
    "pm.test('Los datos coinciden con el producto creado', function () {",
    "    pm.expect(json.id).to.eql(Number(pm.collectionVariables.get('productoId')));",
    "    pm.expect(json.sku).to.eql('INT-001');",
    "    pm.expect(json.nombre).to.eql('Producto Integration');",
    "});",
]

filtrado_tests = [
    "pm.test('GET con filtro devuelve 200', function () {",
    "    pm.response.to.have.status(200);",
    "});",
    "const json = pm.response.json();",
    "pm.test('Todos los items contienen \"Integration\" en el nombre', function () {",
    "    pm.expect(json).to.be.an('array');",
    "    pm.expect(json.length).to.be.greaterThan(0);",
    "    json.forEach(p => {",
    "        pm.expect(p.nombre.toLowerCase()).to.include('integration');",
    "    });",
    "});",
]

put_tests = [
    "pm.test('PUT devuelve 200', function () {",
    "    pm.response.to.have.status(200);",
    "});",
    "const json = pm.response.json();",
    "pm.test('El nombre y precio se han actualizado', function () {",
    "    pm.expect(json.nombre).to.eql('Producto Integration Actualizado');",
    "    pm.expect(json.precioVenta).to.eql(2.5);",
    "});",
]

patch_tests = [
    "pm.test('PATCH devuelve 200', function () {",
    "    pm.response.to.have.status(200);",
    "});",
    "const json = pm.response.json();",
    "pm.test('Solo el stock se ha actualizado, el resto se preserva', function () {",
    "    pm.expect(json.stockTotal).to.eql(50);",
    "    // SKU no se ha tocado en el PATCH, debe seguir siendo el de antes",
    "    pm.expect(json.sku).to.eql('INT-001');",
    "    pm.expect(json.nombre).to.eql('Producto Integration Actualizado');",
    "});",
]

delete_tests = [
    "pm.test('DELETE devuelve 204', function () {",
    "    pm.response.to.have.status(204);",
    "});",
    "pm.test('El cuerpo de la respuesta está vacío', function () {",
    "    pm.expect(pm.response.text()).to.eql('');",
    "});",
]

productos_happy = {
    "name": "1. Productos - Camino feliz",
    "item": [
        make_request(
            "POST crear producto (201)",
            "POST",
            "/api/productos",
            body=('{\n'
                  '  "nombre": "Producto Integration",\n'
                  '  "sku": "INT-001",\n'
                  '  "descripcion": "Producto para tests de integración",\n'
                  '  "precioCosto": 1.0,\n'
                  '  "precioVenta": 1.5,\n'
                  '  "stockTotal": 100\n}'),
            tests=crear_producto_tests,
        ),
        make_request(
            "GET listar productos (200)",
            "GET",
            "/api/productos",
            tests=listar_productos_tests,
        ),
        make_request(
            "GET producto por id (200)",
            "GET",
            "/api/productos/{{productoId}}",
            tests=get_por_id_tests,
        ),
        make_request(
            "GET con filtro nombre=Integration (200)",
            "GET",
            "/api/productos?nombre=Integration",
            tests=filtrado_tests,
        ),
        make_request(
            "PUT actualizar producto (200)",
            "PUT",
            "/api/productos/{{productoId}}",
            body=('{\n'
                  '  "nombre": "Producto Integration Actualizado",\n'
                  '  "sku": "INT-001",\n'
                  '  "precioVenta": 2.5,\n'
                  '  "stockTotal": 100\n}'),
            tests=put_tests,
        ),
        make_request(
            "PATCH parcial - solo stock (200)",
            "PATCH",
            "/api/productos/{{productoId}}",
            body='{\n  "stockTotal": 50\n}',
            tests=patch_tests,
        ),
        make_request(
            "DELETE producto (204)",
            "DELETE",
            "/api/productos/{{productoId}}",
            tests=delete_tests,
        ),
    ],
}


# ---------- PRODUCTOS - CASOS DE ERROR ----------

post_400_tests = [
    "pm.test('POST sin SKU devuelve 400', function () {",
    "    pm.response.to.have.status(400);",
    "});",
    "const json = pm.response.json();",
    "pm.test('La respuesta de error tiene codigo y mensaje', function () {",
    "    pm.expect(json).to.have.property('codigo').that.eql(400);",
    "    pm.expect(json).to.have.property('mensaje').that.is.a('string');",
    "});",
]

get_404_tests = [
    "pm.test('GET de id inexistente devuelve 404', function () {",
    "    pm.response.to.have.status(404);",
    "});",
    "const json = pm.response.json();",
    "pm.test('La respuesta indica que no se encontró el producto', function () {",
    "    pm.expect(json.codigo).to.eql(404);",
    "    pm.expect(json.mensaje.toLowerCase()).to.include('no encontrado');",
    "});",
]

put_404_tests = [
    "pm.test('PUT de id inexistente devuelve 404', function () {",
    "    pm.response.to.have.status(404);",
    "});",
    "pm.test('Body de error coherente', function () {",
    "    pm.expect(pm.response.json().codigo).to.eql(404);",
    "});",
]

patch_404_tests = [
    "pm.test('PATCH de id inexistente devuelve 404', function () {",
    "    pm.response.to.have.status(404);",
    "});",
]

delete_404_tests = [
    "pm.test('DELETE de id inexistente devuelve 404', function () {",
    "    pm.response.to.have.status(404);",
    "});",
]

productos_errores = {
    "name": "2. Productos - Casos de error",
    "item": [
        make_request(
            "POST sin SKU (400)",
            "POST",
            "/api/productos",
            body='{\n  "nombre": "Sin SKU",\n  "precioVenta": 1.0,\n  "stockTotal": 5\n}',
            tests=post_400_tests,
        ),
        make_request(
            "GET id inexistente (404)",
            "GET",
            "/api/productos/999999",
            tests=get_404_tests,
        ),
        make_request(
            "PUT id inexistente (404)",
            "PUT",
            "/api/productos/999999",
            body='{\n  "nombre": "X",\n  "sku": "X",\n  "precioVenta": 1.0,\n  "stockTotal": 1\n}',
            tests=put_404_tests,
        ),
        make_request(
            "PATCH id inexistente (404)",
            "PATCH",
            "/api/productos/999999",
            body='{\n  "stockTotal": 10\n}',
            tests=patch_404_tests,
        ),
        make_request(
            "DELETE id inexistente (404)",
            "DELETE",
            "/api/productos/999999",
            tests=delete_404_tests,
        ),
    ],
}


# ---------- COLECCIÓN ----------

collection = {
    "info": {
        "name": "Inventario API - Integration Tests",
        "description": (
            "Tests de integración pensados para ejecutarse con Newman "
            "(CLI de Postman) tanto en local como en la GitHub Action "
            "de integration-tests.yml. Cubre el flujo completo de la "
            "entidad Producto: alta, consulta, filtrado, actualización, "
            "borrado y los códigos de error principales (400 y 404)."
        ),
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "variable": [
        {"key": "baseUrl", "value": "http://localhost:8080"},
        {"key": "authUsername", "value": "ci-tester"},
        {"key": "authPassword", "value": "ci-secret-123"},
        {"key": "authToken", "value": ""},
        {"key": "productoId", "value": ""},
    ],
    # Auth Bearer global: las requests heredan el header Authorization
    "auth": {
        "type": "bearer",
        "bearer": [{"key": "token", "value": "{{authToken}}", "type": "string"}],
    },
    "item": [auth_folder, productos_happy, productos_errores],
}

OUT.write_text(json.dumps(collection, indent=2, ensure_ascii=False))
total_requests = sum(len(f["item"]) for f in collection["item"])
print(f"OK - {OUT}")
print(f"  - {len(collection['item'])} folders")
print(f"  - {total_requests} requests")
