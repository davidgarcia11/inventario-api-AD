# APIMan — Guía paso a paso

Guía para publicar la **Inventario API** detrás del **API Manager Apiman 3.1**, exigir un **API Token** y aplicar **dos políticas** (API Key Authentication + Rate Limiting). Cubre el Obligatorio #5 de la 2ª evaluación.

```
                              ┌──────────────────────────────────┐
                              │ Apiman Gateway                   │
 Cliente Postman ──── HTTP ──►│  - Valida X-API-Key              │
                              │  - Aplica Rate Limit 100/min     │
                              └────────────────┬─────────────────┘
                                               │  red Docker
                                               │  inventario-net
                                               ▼
                                ┌────────────────────────────────┐
                                │ Inventario API (Spring Boot)   │
                                │  - Lógica CRUD + MariaDB       │
                                └────────────────────────────────┘
```

---

## 0. Prerequisitos

- Docker Desktop arrancado, **~8 GB de RAM libres**.
- ⏱ La primera vez arrancar Apiman tarda **5–10 minutos** (descarga ~4 GB de imágenes + warm-up de Keycloak). Las siguientes veces, ~2 min.

## Sobre este setup

El directorio [`apiman/`](apiman/) contiene **el quickstart oficial de Apiman 3.1.3.Final tal cual** ([apiman.io/download.html](https://www.apiman.io/download.html) → pestaña Docker Compose) con **una sola modificación**: el servicio `apiman-gateway` se conecta también a la red externa `inventario-net` para poder resolver el hostname `inventario-api` y reenviarle el tráfico.

> ⚠️ **Aviso para usuarios de Docker Desktop for Mac (chip M-series)**: el quickstart oficial usa Traefik como reverse-proxy, y Traefik se apoya en su provider docker que en Docker Desktop for Mac **no puede leer correctamente el socket `/var/run/docker.sock`**. Resultado: el Manager UI devuelve 404 en `apiman.local.gd:8080`. Es un problema conocido (ver [Apiman discussions](https://github.com/orgs/apiman/discussions)).
>
> Si tu equipo es Linux o Windows con Docker Desktop, el setup funciona tal cual.
>
> Si estás en Mac y necesitas que arranque sí o sí para una demo, comenta temporalmente el servicio `reverse-proxy` del `docker-compose.yml` y añade `ports: - "7080:8080"` al servicio `apiman-manager` (o cualquier puerto libre).

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

> El dominio `local.gd` es un DNS público que apunta a `127.0.0.1` para cualquier subdominio. No hace falta tocar `/etc/hosts`.

URLs útiles tras el arranque:

| Componente | URL | Credenciales |
|---|---|---|
| **Manager UI** | http://apiman.local.gd:8080/apimanui/ | `admin` / `admin123!` |
| Developer Portal | http://apiman.local.gd:8080/portal/ | — |
| **Gateway** | http://gateway.local.gd:8080/ | — |
| **Keycloak Admin** | http://auth.local.gd:8080/admin/ | `admin` / `admin123!` |
| Mailserver mock | http://mail.local.gd:8080/ | — |

---

## 2. Crear la organización

1. Entra al Manager UI: http://apiman.local.gd:8080/apimanui/
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
http://gateway.local.gd:8080/InventarioOrg/InventarioAPI/1.0
```

Hay que mandar `X-API-Key: <tu-api-key>` en cada petición.

### Sin API key (debe rechazar)
```bash
curl -i http://gateway.local.gd:8080/InventarioOrg/InventarioAPI/1.0/api/productos
# → 403 Forbidden, "API key required"
```

### Con API key
```bash
APIKEY=<pega-aqui-tu-key>

curl -H "X-API-Key: $APIKEY" \
     http://gateway.local.gd:8080/InventarioOrg/InventarioAPI/1.0/api/productos
# → [] HTTP 200
```

### Probar el rate limit (la 2ª política)
Lanza más de 100 peticiones por minuto y a partir de la 101 verás `429 Too Many Requests` devuelto por Apiman, sin llegar a la API:

```bash
for i in $(seq 1 110); do
  curl -s -o /dev/null -w "%{http_code} " \
    -H "X-API-Key: $APIKEY" \
    http://gateway.local.gd:8080/InventarioOrg/InventarioAPI/1.0/api/productos
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
| `apimanGateway` | `http://gateway.local.gd:8080/InventarioOrg/InventarioAPI/1.0` |
| `apiKey` | *(pega aquí la key obtenida en el paso 6)* |

Cada request lleva la cabecera `X-API-Key: {{apiKey}}` para Apiman.

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
| `404` en `apiman.local.gd:8080` en Mac | Traefik no puede leer el socket Docker en Docker Desktop for Mac | Ver el aviso en la sección "Sobre este setup". Mapear puerto directo al manager. |
| Login redirige y vuelve sin entrar | Keycloak no tiene cookie válido | Comprueba `KC_HOSTNAME` y `KC_HOSTNAME_PORT` en el compose |
| Gateway responde 404 al llamar a `/api/...` | Falta publicar la API o crear el contrato | Repite pasos 5 y 6 |
| Gateway responde 502 Bad Gateway | El gateway no llega a `inventario-api:8080` | `docker network inspect inventario-net`: verifica que `inventario-api` y `apiman-apiman-gateway-1` están en la red |
| Gateway responde 401 sin pasar a la API | Falta `X-API-Key` o no coincide | Revisa la key en `Clients → PostmanClient → APIs` |
| Gateway responde 429 al primer request | El rate limit ya está aplicado | Espera 1 minuto o sube el límite en el plan |
