#!/usr/bin/env python3
"""
Genera la colección Postman "Inventario API - via Apiman".

Las requests apuntan al gateway de Apiman (puerto 7080) en vez de a la
API directamente (8080). Cada request lleva:
- X-API-Key   → la valida el gateway antes de reenviar a la API.
- Bearer JWT  → la valida la API (Spring Security) cuando recibe la
                petición ya filtrada por Apiman.

El pre-request script de la colección renueva el JWT automáticamente
(igual que la colección principal). La API key la pega el alumno en una
variable de colección tras haberla obtenido en el Manager de Apiman.
"""
import json
from pathlib import Path

OUT = Path("Inventario API - via Apiman.postman_collection.json")


def make_req(name, method, path, *, body=None, description="", auth_noauth=False):
    """Construye una request Postman v2.1.0."""
    parsed = path.split("?", 1)
    url_path = parsed[0]
    query = []
    if len(parsed) == 2:
        for kv in parsed[1].split("&"):
            k, v = kv.split("=", 1)
            query.append({"key": k, "value": v})

    req = {
        "name": name,
        "request": {
            "method": method,
            "header": [
                # Cabecera obligatoria para Apiman. La pone manualmente para
                # que sea visible al alumno; la API key vive en variable.
                {"key": "X-API-Key", "value": "{{apiKey}}"}
            ],
            "url": {
                "raw": "{{apimanGateway}}" + path,
                "host": ["{{apimanGateway}}"],
                "path": [p for p in url_path.split("/") if p],
            },
            "description": description,
        },
        "response": [],
    }
    if query:
        req["request"]["url"]["query"] = query
    if body is not None:
        req["request"]["header"].append(
            {"key": "Content-Type", "value": "application/json"}
        )
        req["request"]["body"] = {"mode": "raw", "raw": body}
    if auth_noauth:
        # Las requests de auth (register/login) van sin Bearer; sí llevan API key
        req["request"]["auth"] = {"type": "noauth"}
    return req


# ---------- Pre-request global ----------
# Renueva el JWT si no hay o está próximo a caducar. Igual que en la
# colección principal pero adaptado: en lugar de llamar a la API
# directamente, hace login a través del gateway de Apiman.
prerequest = [
    "// Si no hay token JWT o falta menos de 1 minuto para que expire,",
    "// hacemos login automáticamente para que el resto de requests no fallen.",
    "// El login también pasa por Apiman (así Apiman ve el tráfico de login).",
    "",
    "const skipNames = ['Register', 'Login'];",
    "if (skipNames.some(n => pm.info.requestName && pm.info.requestName.includes(n))) {",
    "    return;",
    "}",
    "",
    "const ahora = Date.now();",
    "const expiraEn = Number(pm.collectionVariables.get('authTokenExpiresAt')) || 0;",
    "const token = pm.collectionVariables.get('authToken');",
    "",
    "if (token && expiraEn > ahora + 60 * 1000) {",
    "    return;",
    "}",
    "",
    "const gateway = pm.collectionVariables.get('apimanGateway');",
    "const apiKey = pm.collectionVariables.get('apiKey');",
    "",
    "pm.sendRequest({",
    "    url: gateway + '/api/auth/login',",
    "    method: 'POST',",
    "    header: {",
    "        'Content-Type': 'application/json',",
    "        'X-API-Key': apiKey",
    "    },",
    "    body: {",
    "        mode: 'raw',",
    "        raw: JSON.stringify({",
    "            username: pm.collectionVariables.get('authUsername'),",
    "            password: pm.collectionVariables.get('authPassword')",
    "        })",
    "    }",
    "}, (err, res) => {",
    "    if (err || res.code !== 200) {",
    "        console.error('Login a través de Apiman ha fallado:', err || res.text());",
    "        return;",
    "    }",
    "    const json = res.json();",
    "    pm.collectionVariables.set('authToken', json.token);",
    "    pm.collectionVariables.set('authTokenExpiresAt', String(Date.now() + json.expiresIn * 1000));",
    "    console.log('Token JWT renovado vía Apiman');",
    "});",
]

# ---------- Tests scripts ----------
# Para Register y Login: guardar token si la respuesta es 200
save_token_test = [
    "if (pm.response.code === 200) {",
    "    const json = pm.response.json();",
    "    pm.collectionVariables.set('authToken', json.token);",
    "    pm.collectionVariables.set('authTokenExpiresAt', String(Date.now() + json.expiresIn * 1000));",
    "}",
]


# ---------- Folders ----------

auth_folder = {
    "name": "0. Auth (via Apiman)",
    "description": (
        "Endpoints de autenticación expuestos a través del gateway. La API "
        "key se pasa siempre; el token JWT se genera con el primer login."
    ),
    "item": [
        {
            **make_req(
                "Register (1ª vez)",
                "POST",
                "/api/auth/register",
                body='{\n  "username": "{{authUsername}}",\n  "password": "{{authPassword}}"\n}',
                description=(
                    "Crea el usuario en la BD. Si ya existía devolverá 400. "
                    "Si la respuesta es 200, el test guarda automáticamente "
                    "el token en {{authToken}}."
                ),
                auth_noauth=True,
            ),
            "event": [
                {
                    "listen": "test",
                    "script": {"type": "text/javascript", "exec": save_token_test},
                }
            ],
        },
        {
            **make_req(
                "Login",
                "POST",
                "/api/auth/login",
                body='{\n  "username": "{{authUsername}}",\n  "password": "{{authPassword}}"\n}',
                description=(
                    "Pide un nuevo token JWT y lo guarda en {{authToken}}."
                ),
                auth_noauth=True,
            ),
            "event": [
                {
                    "listen": "test",
                    "script": {"type": "text/javascript", "exec": save_token_test},
                }
            ],
        },
    ],
}


productos_folder = {
    "name": "1. Productos (via Apiman)",
    "description": (
        "Operaciones CRUD sobre Productos pasando por el gateway. "
        "Apiman valida la API Key y aplica rate limit; la API valida el JWT."
    ),
    "item": [
        make_req(
            "POST crear producto",
            "POST",
            "/api/productos",
            body=('{\n'
                  '  "nombre": "Producto Apiman",\n'
                  '  "sku": "APIMAN-001",\n'
                  '  "precioVenta": 2.0,\n'
                  '  "stockTotal": 25\n}'),
        ),
        make_req(
            "GET listar productos",
            "GET",
            "/api/productos",
        ),
        make_req(
            "GET con filtro nombre",
            "GET",
            "/api/productos?nombre=Apiman",
        ),
        make_req(
            "PUT actualizar producto",
            "PUT",
            "/api/productos/1",
            body=('{\n'
                  '  "nombre": "Producto Apiman Actualizado",\n'
                  '  "sku": "APIMAN-001",\n'
                  '  "precioVenta": 3.0,\n'
                  '  "stockTotal": 25\n}'),
        ),
        make_req(
            "PATCH actualizar parcial",
            "PATCH",
            "/api/productos/1",
            body='{\n  "stockTotal": 10\n}',
        ),
        make_req(
            "DELETE producto",
            "DELETE",
            "/api/productos/1",
        ),
    ],
}


rate_limit_folder = {
    "name": "2. Probar políticas de Apiman",
    "description": (
        "Requests pensadas para ver las dos políticas en acción: API Key "
        "(quitando la cabecera) y Rate Limiting (lanzando muchas peticiones)."
    ),
    "item": [
        {
            "name": "Sin API Key (espera 403)",
            "request": {
                "method": "GET",
                # No mandamos X-API-Key adrede
                "header": [],
                "url": {
                    "raw": "{{apimanGateway}}/api/productos",
                    "host": ["{{apimanGateway}}"],
                    "path": ["api", "productos"],
                },
                "auth": {"type": "noauth"},
                "description": (
                    "Apiman rechaza la petición porque no llega X-API-Key. "
                    "Demuestra que la política API Key está activa."
                ),
            },
            "response": [],
        },
        make_req(
            "Rate limit (lanzar varias veces seguidas)",
            "GET",
            "/api/productos",
            description=(
                "Si lanzas más de 100 de estas en menos de un minuto, "
                "Apiman responderá con 429 Too Many Requests. "
                "Demuestra que la política de Rate Limiting está activa."
            ),
        ),
    ],
}


# ---------- Colección ----------

collection = {
    "info": {
        "name": "Inventario API - via Apiman",
        "description": (
            "Versión de la colección preparada para pasar por el gateway "
            "de Apiman en lugar de hablar directamente con la API. Cada "
            "petición lleva una API Key (validada por Apiman) y un JWT "
            "(validado por Spring Security). El pre-request script renueva "
            "el JWT automáticamente; la API Key la pone el alumno en una "
            "variable de colección tras haberla obtenido en el Manager."
        ),
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "variable": [
        {
            "key": "apimanGateway",
            "value": "http://gateway.local.gd:8080/InventarioOrg/InventarioAPI/1.0",
            "description": "URL base del gateway de Apiman para esta API.",
        },
        {
            "key": "apiKey",
            "value": "<pega-aqui-tu-api-key>",
            "description": "API Key obtenida en el Manager (Clients > PostmanClient > APIs).",
        },
        {"key": "authUsername", "value": "david"},
        {"key": "authPassword", "value": "secreto123"},
        {"key": "authToken", "value": ""},
        {"key": "authTokenExpiresAt", "value": "0"},
    ],
    "auth": {
        "type": "bearer",
        "bearer": [{"key": "token", "value": "{{authToken}}", "type": "string"}],
    },
    "event": [
        {
            "listen": "prerequest",
            "script": {"type": "text/javascript", "exec": prerequest},
        }
    ],
    "item": [auth_folder, productos_folder, rate_limit_folder],
}

OUT.write_text(json.dumps(collection, indent=2, ensure_ascii=False))
total = sum(len(f["item"]) for f in collection["item"])
print(f"OK - {OUT}")
print(f"  - {len(collection['item'])} folders, {total} requests")
print(f"  - Pre-request global: renueva JWT vía Apiman cuando hace falta")
print(f"  - auth Bearer global con {{{{authToken}}}}")
