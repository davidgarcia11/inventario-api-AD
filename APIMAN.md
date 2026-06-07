# APIMan — Guía paso a paso

Esta guía explica cómo publicar la **Inventario API** detrás del **API Manager Apiman 3.1** (versión oficial), exigir un **API token** y aplicar **dos políticas** (API Key Authentication + Rate Limiting), tal y como pide el Obligatorio #5 de la 2ª evaluación.

```
                              ┌──────────────────────────────────┐
                              │ Apiman Gateway (localhost:7081)  │
 Cliente Postman ──── HTTP ──►│  - Valida X-API-Key              │
                              │  - Aplica Rate Limit 100/min     │
                              └────────────────┬─────────────────┘
                                               │  (red Docker
                                               │   inventario-net)
                                               ▼
                                ┌────────────────────────────────┐
                                │ Inventario API (Spring Boot)   │
                                │  - Valida JWT                  │
                                │  - Lógica CRUD + MariaDB       │
                                └────────────────────────────────┘
```

---

## 0. Prerequisitos

- Docker Desktop arrancado, ~8 GB de RAM libres.
- ⏱ La primera vez arrancar Apiman tarda **5–10 minutos** (descarga ~4 GB de imágenes + warm-up de Keycloak). Las siguientes veces, ~2 minutos.

> ⚠️ En Mac con chip M-series (ARM64) las imágenes son amd64 → se ejecutan en emulación. Es esperable que vaya más lento que en Linux/Windows nativo.

El stack está construido sobre el [quickstart oficial Apiman 3.1.3.Final](https://github.com/apiman/apiman/releases/tag/3.1.3.Final) con dos modificaciones:

1. **Eliminado Traefik** (el reverse-proxy). El provider docker de Traefik no funciona bien con el socket de Docker Desktop for Mac. En su lugar exponemos cada servicio en puertos directos del host.
2. El gateway de Apiman se enchufa también a la red `inventario-net` para resolver el hostname `inventario-api`.

---

## 1. Arrancar el stack

Necesitamos primero la API + MariaDB y, después, Apiman.

```bash
# 1) Stack principal (API + MariaDB) en la raíz del repo
cp .env.example .env       # solo la primera vez
docker compose up -d --build

# 2) Inicializar secretos y claves RSA de Apiman (solo la primera vez)
cd apiman
cp .env.example .env       # solo la primera vez
docker compose -f docker-compose.setup.yml up
# → "init-1 exited with code 0"  cuando termine

# 3) Levantar el stack de Apiman
docker compose up -d

# 4) Esperar a que el manager esté healthy (5-10 min la primera vez)
docker compose ps
# → "apiman-apiman-manager-1 ... (healthy)"
```

URLs útiles tras el arranque:

| Componente | URL | Credenciales |
|---|---|---|
| **Manager UI** | http://localhost:7080/apimanui/ | `admin` / `admin123!` |
| Manager API | http://localhost:7080/apiman/system/status | — |
| **Gateway** | http://localhost:7081/ | — |
| **Keycloak Admin** | http://localhost:7082/admin/ | `admin` / `admin123!` |
| Mailserver mock | (no expuesto) | — |

---

## 2. Crear la organización

1. Entra al Manager UI: http://localhost:7080/apimanui/
2. Login con `admin` / `admin123!` (te redirige a Keycloak).
3. **New Organization** (esquina superior derecha).
4. Rellena:
   - **Name**: `InventarioOrg`
   - **Description**: `Organización para la API de inventario`
5. **Create Organization**.

---

## 3. Registrar la API

1. Dentro de `InventarioOrg`, pestaña **APIs → New API**.
2. Rellena:
   - **Name**: `InventarioAPI`
   - **Initial Version**: `1.0`
3. **Create API**.
4. En el detalle de la API, pestaña **Implementation**:
   - **API Endpoint**: `http://inventario-api:8080`
     > `inventario-api` es el hostname interno del contenedor de la API en la red Docker `inventario-net`. El gateway de Apiman lo resuelve porque también está enchufado a esa red.
   - **API Type**: `REST`
5. Marca **Public API** = `false` (forzamos a usar plan).
6. **Save**.

---

## 4. Crear el plan y añadir las dos políticas

Un **Plan** es un conjunto de políticas que se aplicarán a quien lo contrate.

1. Pestaña **Plans → New Plan**.
2. Rellena:
   - **Name**: `Basic`
   - **Initial Version**: `1.0`
3. **Create Plan**.
4. Dentro del plan, pestaña **Policies → Add Policy**.

### 4.1 Política 1: API Key

- **Policy Type**: `API Key`
- Por defecto exige el header `X-API-Key` o el query param `apikey`.
- **Add**.

### 4.2 Política 2: Rate Limiting

- **Policy Type**: `Rate Limiting`
- Configura:
  - **Maximum Requests**: `100`
  - **Time period**: `Minute`
  - **Granularity**: `Api`
  - **Limiting strategy**: `Strict`
- **Add**.

5. **Lock Plan** para fijar la versión 1.0.

---

## 5. Asociar el plan a la API y publicarla

1. Vuelve a la API `InventarioAPI 1.0`.
2. Pestaña **Plans → marca `Basic 1.0` → Save**.
3. Pestaña **Endpoint**: comprueba que la URL backend sigue siendo `http://inventario-api:8080`.
4. Botón **Publish** (esquina superior derecha). Apiman registra la API en el gateway.

---

## 6. Crear cliente y obtener API Key

1. Pestaña **Clients → New Client**.
2. Rellena:
   - **Name**: `PostmanClient`
   - **Initial Version**: `1.0`
3. **Create Client**.
4. Pestaña **Contracts → New Contract**:
   - **Organization**: `InventarioOrg`
   - **API**: `InventarioAPI 1.0`
   - **Plan**: `Basic 1.0`
5. **Create Contract**.
6. **Register Client** (esquina superior derecha).

Una vez registrado, en la pestaña **APIs** del cliente verás un campo **API Key** con un valor tipo `abcd-1234-...`. **Copia esa key**, la usaremos en las peticiones.

---

## 7. Probar el gateway

La URL del gateway para nuestra API es:

```
http://localhost:7081/InventarioOrg/InventarioAPI/1.0
```

Hay que mandar `X-API-Key: <tu-api-key>` en cada petición.

### Sin API key (debe rechazar)
```bash
curl -i http://localhost:7081/InventarioOrg/InventarioAPI/1.0/api/auth/register \
  -X POST -H "Content-Type: application/json" \
  -d '{"username":"david","password":"secreto123"}'
# → 403 Forbidden, "API key required"
```

### Con API key (debe pasar y devolver el JWT)
```bash
APIKEY=<pega-aqui-tu-key>

curl -X POST \
  -H "X-API-Key: $APIKEY" \
  -H "Content-Type: application/json" \
  -d '{"username":"david","password":"secreto123"}' \
  http://localhost:7081/InventarioOrg/InventarioAPI/1.0/api/auth/register
# → { "token": "eyJ...", "username": "david", "expiresIn": 3600 }
```

### Llamada autenticada (API key + JWT)
```bash
TOKEN=<pega-aqui-el-token-jwt>

curl -H "X-API-Key: $APIKEY" \
     -H "Authorization: Bearer $TOKEN" \
     http://localhost:7081/InventarioOrg/InventarioAPI/1.0/api/productos
# → [] HTTP 200
```

> Las dos protecciones actúan en cascada: Apiman valida la API Key en el gateway; nuestra API valida el JWT con Spring Security. Si falta cualquiera de los dos, la petición falla.

### Probar el rate limit (la 2ª política)
Lanza más de 100 peticiones por minuto y a partir de la 101 verás `429 Too Many Requests` devuelto por Apiman, sin llegar a la API:

```bash
for i in $(seq 1 110); do
  curl -s -o /dev/null -w "%{http_code} " \
    -H "X-API-Key: $APIKEY" \
    -H "Authorization: Bearer $TOKEN" \
    http://localhost:7081/InventarioOrg/InventarioAPI/1.0/api/productos
done
# → 200 200 ... 200 429 429 429
```

---

## 8. Colección Postman para usar a través de Apiman

En el repo hay una colección dedicada:
**`Inventario API - via Apiman.postman_collection.json`**.

Variables de colección:

| Variable | Valor por defecto |
|---|---|
| `apimanGateway` | `http://localhost:7081/InventarioOrg/InventarioAPI/1.0` |
| `apiKey` | *(pega aquí la key obtenida en el paso 6)* |
| `authUsername` | `david` |
| `authPassword` | `secreto123` |
| `authToken` | *(se rellena automáticamente con el pre-request script)* |

Cada request lleva:
- `X-API-Key: {{apiKey}}` → para Apiman.
- `Authorization: Bearer {{authToken}}` → para Spring Security, lo gestiona el pre-request script.

El folder **"2. Probar políticas de Apiman"** incluye:
- Una request **sin** `X-API-Key` para que veas que el gateway responde 403.
- Una request para lanzar muchas veces a mano y disparar el rate limit (429 a partir de la 101).

---

## Apagar el stack

```bash
# Solo Apiman
cd apiman && docker compose down

# Stack principal
cd .. && docker compose down

# Si quieres limpiar también los volúmenes (BD vacía la próxima vez)
docker compose down -v
cd apiman && docker compose down -v
```

---

## Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| Manager UI tarda en cargar | Apiman aún arrancando | Espera 5 min y `docker compose ps` para ver healthcheck |
| `404` al entrar al manager | Apiman aún no terminó el warm-up | Espera, en ARM puede tardar más |
| Login al manager redirige y vuelve | Keycloak no le da el cookie | Comprueba `KC_HOSTNAME` y `KC_HOSTNAME_PORT` en el compose |
| Gateway responde 404 al llamar a `/api/...` | Falta publicar la API o crear el contrato | Repite pasos 5 y 6 |
| Gateway responde 502 Bad Gateway | El gateway no llega a `inventario-api:8080` | `docker network inspect inventario-net` para verificar que el contenedor de la API y el de Apiman gateway están ahí |
| Gateway responde 401 sin pasar a la API | Falta `X-API-Key` o no coincide | Revisa la key en `Clients → PostmanClient → APIs` |
| Gateway responde 429 al primer request | El rate limit ya está aplicado | Espera 1 minuto o sube el límite en el plan |
