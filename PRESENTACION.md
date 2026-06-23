# PRESENTACION.md — Guion de defensa 2ª evaluación

Documento pensado como **chuleta el día de la defensa**. Cubre los 5 obligatorios + extras en el orden en el que se enseñan al profesor. Si todo va bien, dura **15–20 minutos**.

---

## 0. Pre-condiciones (hacer ANTES de entrar al aula)

| Comprobación | Cómo se verifica |
|---|---|
| ✅ Portátil Ubuntu encendido, batería al 50%+ y cargador a mano | — |
| ✅ Repo clonado en `~/Projects/inventario-api-AD` y en rama `develop` | `cd ~/Projects/inventario-api-AD && git status` |
| ✅ EC2 arrancada en AWS Academy Learner Lab | Web AWS → EC2 → estado `Running`, `2/2 checks` |
| ✅ IP pública de la EC2 apuntada en el environment `aws` de Postman | Postman → Environments → `aws` → `baseUrl` |
| ✅ API en AWS responde | `curl http://NUEVA_IP:8080/api/info` devuelve JSON con `perfilActivo: docker` |
| ✅ Datos de muestra cargados en AWS | `newman run deploy/seed-aws-almacenes.postman_collection.json -e aws.postman_environment.json` |
| ✅ Apiman levantado en local | `cd apiman && docker compose ps` — todos `healthy` |
| ✅ Apiman tiene la API publicada con backend = la IP de AWS | Manager UI → `InventarioAPI 1.0` → Implementation → `http://NUEVA_IP:8080` |
| ✅ Postman con los 4 collections y los 2 environments importados | Postman → Workspace |
| ✅ IntelliJ con el proyecto abierto y sin errores | IntelliJ |

---

## 1. Pestañas/ventanas a tener abiertas

Antes de empezar, abre estas pestañas/ventanas para no perder tiempo durante la defensa:

**Navegador (Firefox/Chrome):**

1. AWS Console → EC2 → tu instancia `inventario-api` (para enseñar que está en AWS)
2. `http://NUEVA_IP:8080/swagger-ui.html` — Swagger de la API en AWS
3. `http://NUEVA_IP:8080/api/info` — endpoint de perfil
4. `https://github.com/davidgarcia11/inventario-api-AD` — repo y PRs/Actions
5. `http://apiman.local.gd:8080/apimanui/` — Manager UI de Apiman

**Aplicaciones:**

- IntelliJ con el proyecto abierto (para abrir DataSeederConfig, AlmacenV2Controller y SecurityConfig rápido)
- Postman con los environments `local` y `aws` y las 4 collections
- 2 terminales abiertas:
  - Terminal 1: en `~/Projects/inventario-api-AD`
  - Terminal 2: en `~/Projects/inventario-api-AD/apiman`

---

## 2. Guion paso a paso (lo que se le enseña al profesor)

### Bloque A — Repositorio y Git Flow (~1 min)

Abrir GitHub → repo → pestaña **Pull requests** (Closed).

> "He seguido un Git Flow estricto: una feature por PR, todas mergeadas a `develop` y luego releases a `main`. CI corre en cada PR con Newman. Todos los obligatorios y extras tienen su propio PR cerrado."

Enseñar la pestaña **Actions** para que vea los runs de CI verdes.

---

### Bloque B — Obligatorio #2: Perfiles dev/prod (~2 min)

**B1. Perfiles distintos a nivel de comportamiento.**

Abrir en el navegador:

```
http://NUEVA_IP:8080/api/info
```

Respuesta esperada:

```json
{
  "nombreApp": "inventario-api-AD",
  "perfilActivo": "docker",
  "descripcion": "Modo Docker: ddl-auto=update, credenciales desde el .env del contenedor, sin datos de muestra.",
  "datosDeMuestraAlArrancar": false
}
```

> "En AWS arranca con perfil `docker`. El endpoint `/api/info` describe qué hace cada perfil. Aquí `datosDeMuestraAlArrancar` es `false` porque solo el perfil `dev` (que uso en local con IntelliJ) carga el DataSeeder."

**B2. Enseñar el código.**

IntelliJ → abrir [DataSeederConfig.java](src/main/java/com/example/inventarioapiad/config/DataSeederConfig.java):

> "La clase está anotada con `@Profile("dev")`, así que solo se carga si el perfil activo es `dev`. En `dev` se crean 3 almacenes de muestra; en `docker` y `prod` no."

Abrir `src/main/resources/application-dev.properties` y `application-prod.properties`:

> "Cada perfil tiene su propio archivo con `ddl-auto` distinto: `create-drop` en dev (BD se rehace en cada arranque), `validate` en prod (la BD no se toca, solo se valida el esquema)."

---

### Bloque C — Obligatorio #1: Versionado V2 (~3 min)

**C1. Enseñar Swagger.**

Navegador → `http://NUEVA_IP:8080/swagger-ui.html`

> "Aquí están los 4 endpoints versionados. La V2 introduce el campo `prioritario` en Almacén. Mismo recurso, distinto contrato:
> - V1: `/api/almacenes` — comportamiento original.
> - V2: `/api/v2/almacenes` — añade `prioritario`, paginación en GET y bloqueo del DELETE si es prioritario."

**C2. Enseñar el código.**

IntelliJ → [AlmacenV2Controller.java](src/main/java/com/example/inventarioapiad/controller/v2/AlmacenV2Controller.java):

> "El controller V2 usa DTOs propios (`AlmacenCreateRequestV2`, `AlmacenUpdateRequestV2`) para aceptar `prioritario`. El DELETE invoca `eliminarSiNoPrioritario(id)` del service: si está marcado como prioritario, lanza `IllegalStateException` que el controller traduce a `409 Conflict`."

**C3. Demostración en Postman contra AWS.**

Postman → environment **aws** seleccionado → collection **"Inventario API - COMPLETA"**:

1. `GET /api/v2/almacenes` → ver respuesta paginada con `content[]` y `prioritario` en cada elemento.
2. `DELETE /api/v2/almacenes/{id}` donde el id es el del almacén "Crítico Madrid" (prioritario) → respuesta **409** con `"mensaje": "No se puede eliminar un almacén prioritario..."`.
3. `PUT /api/v2/almacenes/{id}` con `prioritario: false` → 200.
4. Reintenta el `DELETE` → **204**.

> "El versionado V2 no es cosmético: cambia el comportamiento de los 4 endpoints alrededor del nuevo campo."

---

### Bloque D — Obligatorio #4: Tests de integración + CI (~3 min)

**D1. Tests en Postman.**

Postman → environment **aws** → collection **"Inventario API - Integration Tests"** → click en la colección entera → **Run**.

- Selecciona todas las requests
- Pulsa **Run Inventario API - Integration Tests**
- Newman corre y enseña los `pm.test()` en verde

> "La colección cubre Productos V1 (camino feliz y casos de error 400/404) y Almacenes V2 (POST normal, POST prioritario, GET paginado, DELETE 409 cuando es prioritario, PUT despriorizar, DELETE 204)."

**D2. Mismos tests por línea de comandos (Newman).**

Terminal 1:

```bash
newman run "Inventario API - Integration Tests.postman_collection.json" -e aws.postman_environment.json
```

> "Mismo resultado que en la UI. Lo importante es que estos tests son los que GitHub Actions ejecuta en cada PR."

**D3. CI en GitHub Actions.**

Navegador → repo → **Actions** → workflow **"Integration tests (Newman + MariaDB)"**.

> "GitHub Actions arranca un MariaDB como contenedor sidecar, construye el JAR, levanta la API y ejecuta Newman contra ella. Si algo falla en cualquier rama, no se puede mergear."

---

### Bloque E — Obligatorio #3: Despliegue en AWS (~2 min)

Ya está enseñado a nivel de funcionamiento (todas las llamadas anteriores van contra AWS). Ahora se enseña el cómo:

**E1. La instancia EC2.**

Navegador → AWS Console → EC2 → instancia `inventario-api`.

> "Es una EC2 t3.small en Amazon Linux 2023. Tiene tres puertos abiertos: 22 (SSH), 8080 (API) y 3306 (BD)."

**E2. El `user-data.sh` que la auto-configura.**

IntelliJ o terminal:

```bash
cat deploy/user-data.sh
```

> "Este script lo pego en el campo 'User data' de AWS al crear la instancia. AWS lo ejecuta como root en el primer arranque: instala Docker, clona el repo y lanza `docker compose up -d` con perfil `docker`. En 5 minutos la API está en línea, sin tocarla a mano."

---

### Bloque F — Obligatorio #5: Apiman con dos políticas (~5 min)

**F1. Apiman levantado en local.**

Terminal 2:

```bash
cd ~/Projects/inventario-api-AD/apiman
docker compose ps
```

Enseñar los servicios `healthy`.

**F2. Manager UI.**

Navegador → `http://apiman.local.gd:8080/apimanui/` → login `admin` / `admin123!`.

> "He creado una organización `InventarioOrg` con una API `InventarioAPI 1.0`. El endpoint backend apunta a la EC2: `http://NUEVA_IP:8080`."

Navegar a `InventarioOrg → APIs → InventarioAPI 1.0 → Implementation` y enseñar el backend.

**F3. Las dos políticas.**

Navegar a `InventarioOrg → Plans → Basic 1.0 → Policies`:

- **API Key Authentication** — exige cabecera `X-API-Key`
- **Rate Limiting** — 100 peticiones/minuto por API

> "Cualquier petición que pase por el gateway debe llevar la API key. Si pasa de 100 al minuto, Apiman corta con un 429 sin llegar a tocar la API."

**F4. Cliente con contrato y API key.**

Navegar a `InventarioOrg → Clients → PostmanClient → APIs` y enseñar la **API key** del contrato.

**F5. Demostración: petición sin key vs con key.**

Terminal 1:

```bash
# Sin key → 401/403
curl -i http://gateway.local.gd:8080/InventarioOrg/InventarioAPI/1.0/api/almacenes

# Con key → 200 (con los almacenes cargados por el seed)
APIKEY=<pega-aqui-tu-key>
curl -H "X-API-Key: $APIKEY" \
  http://gateway.local.gd:8080/InventarioOrg/InventarioAPI/1.0/api/almacenes
```

**F6. Mismo flujo desde Postman.**

Postman → collection **"Inventario API - via Apiman"** → variables `apimanGateway` y `apiKey` actualizadas → ejecutar:

- Folder **"1. Llamadas normales con X-API-Key"** → 200
- Folder **"2. Probar políticas de Apiman"** → sin key da 401/403, repetido 110 veces dispara 429

---

### Bloque G — Extras (~3 min)

**G1. Docker Compose local (extra).**

> "El proyecto tiene su propio `docker-compose.yml` en raíz que monta la API + MariaDB. Es lo mismo que corre en la EC2: si yo `docker compose up -d` en mi portátil, tengo exactamente el mismo entorno que en producción."

**G2. JWT (extra).**

> "Implementé también seguridad JWT (`/api/auth/register`, `/api/auth/login`). Está en el código pero desactivada a nivel de filtros (`SecurityConfig` con `permitAll`) para que la defensa con Postman sea fluida. Está pensado así para no chocar con Apiman, que ya impone su propia seguridad por API key."

Abrir [SecurityConfig.java](src/main/java/com/example/inventarioapiad/security/SecurityConfig.java).

---

## 3. Preguntas trampa probables del profesor y cómo responder

| Pregunta | Respuesta |
|---|---|
| "¿Por qué la V2 si la V1 sigue funcionando?" | Para introducir un cambio incompatible (campo `prioritario` y bloqueo del DELETE) sin romper clientes existentes. La V1 sigue sirviendo sin lógica nueva. |
| "¿Por qué arranca vacío en AWS?" | Porque el perfil `docker` no tiene `DataSeederConfig` activado (`@Profile("dev")`). Cargar datos solo tiene sentido en desarrollo. |
| "¿Cómo cierras la trazabilidad de qué pasa en cada perfil?" | El endpoint `/api/info` describe en texto el comportamiento del perfil activo y los `application-*.properties` documentan qué cambia (`ddl-auto`, datasource). |
| "¿Quién valida que Apiman corta el rate limit?" | Las requests `for i in $(seq 1 110)` o el folder 2 de la colección "via Apiman". A partir de la 101 devuelve 429 sin llegar a la API. |
| "¿Por qué tienes el backend de Apiman apuntando a AWS y no a un contenedor local?" | Porque la API real está desplegada en AWS (Obligatorio #3) y así el gateway protege exactamente esa instancia. Si pongo el backend local, no demuestro el escenario completo. |

---

## 4. Si algo falla en directo

| Síntoma | Plan B |
|---|---|
| La EC2 no responde | Plan B: levantar la API local con `docker compose up -d` y cambiar el environment de Postman a `local`. Apuntar también el backend de Apiman a `http://172.17.0.1:8080` (gateway → host). |
| Apiman no arranca o el manager devuelve 502 | Saltar al bloque sin Apiman, decir "el setup oficial está en `APIMAN.md`" y enseñar la colección **via Apiman** comparada con la **COMPLETA** sin ejecutar (capturas en el README pueden ayudar). |
| Newman explota en Postman | Ejecutar la misma colección por CLI. Si CLI tampoco va, mostrar el último run verde en GitHub Actions como evidencia. |

---

## 5. Cierre

> "En resumen: 5 obligatorios cubiertos con PRs cerrados, CI verde y la API funcionando tanto en local como en AWS. Los extras (Docker Compose local y JWT) añaden contexto al diseño. La V2 da sentido al versionado eligiendo un campo (`prioritario`) que cambia el contrato de los 4 endpoints. Apiman protege esa API con dos políticas. Cualquier cosa que mostraste hoy está reproducible en cualquier máquina con `git clone` + `docker compose up`."
