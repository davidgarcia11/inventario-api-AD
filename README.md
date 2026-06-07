# Inventario API - Spring Boot

API REST para gestión de inventario con Spring Boot 3.2.2 y MariaDB.

**Estado:** ✅ Primera entrega completada

## 📋 Requisitos

- Java 21
- Gradle 8.5
- MariaDB 12.1+
- Postman (para probar endpoints)

## 🚀 Cómo ejecutar

### 1. Configurar Base de Datos
```bash
mysql -u root -p
CREATE DATABASE inventario_db;
```

### 2. Clonar el proyecto
```bash
git clone https://github.com/davidgarcia11/inventario-api-AD.git
cd inventario-api-AD
```

### 3. Ejecutar tests
```bash
./gradlew test
```

Resultado esperado: **BUILD SUCCESSFUL** (106+ tests pasando)

### 4. Ejecutar la aplicación
```bash
./gradlew bootRun
```

La API estará disponible en: `http://localhost:8080`

## ⚙️ Perfiles de configuración

La configuración está dividida en tres ficheros:

| Fichero | Propósito |
|---|---|
| `application.properties` | Configuración común y elección del perfil por defecto (`dev`) |
| `application-dev.properties` | Desarrollo local: MariaDB en `localhost:3306`, `root/root`, esquema recreado en cada arranque |
| `application-prod.properties` | Producción: credenciales y URL inyectadas vía variables de entorno, `ddl-auto=validate` |

### Arrancar en modo desarrollo (perfil por defecto)
```bash
./gradlew bootRun
```

### Arrancar en modo producción
Define las variables de entorno y activa el perfil `prod`:
```bash
export DB_URL=jdbc:mariadb://mi-host-prod:3306/inventario_db
export DB_USERNAME=mi_usuario
export DB_PASSWORD=mi_password_seguro
./gradlew bootRun --args='--spring.profiles.active=prod'
```

O ejecutando el JAR construido:
```bash
SPRING_PROFILES_ACTIVE=prod \
  DB_URL=jdbc:mariadb://mi-host-prod:3306/inventario_db \
  DB_USERNAME=mi_usuario \
  DB_PASSWORD=mi_password_seguro \
  java -jar build/libs/inventario-api-AD-0.0.1-SNAPSHOT.jar
```

Si falta cualquiera de las variables, la aplicación falla al arrancar (intencionado).

## 🐳 Ejecución con Docker

### Stack completo (API + MariaDB)
```bash
cp .env.example .env          # ajusta usuario/contraseña antes de subir
docker compose up --build     # la primera vez compila la imagen
```
- API: http://localhost:8080
- MariaDB: `localhost:3307` (no choca con la MariaDB local si la tienes en 3306)
- Perfil Spring activo: `docker` (`ddl-auto=update`, crea esquema la primera vez)

### Solo BD para desarrollo local
Si prefieres correr la API con `./gradlew bootRun` y solo necesitas la BD en contenedor:
```bash
docker compose -f docker-compose.dev.yml up -d
./gradlew bootRun                  # perfil dev por defecto
```
Esto expone MariaDB en `localhost:3306` con `root/root` e `inventario_db` ya creada, justo lo que el perfil `dev` espera.

### Parar y limpiar
```bash
docker compose down              # para el stack, conserva los datos
docker compose down -v           # también borra el volumen (BD vacía la próxima vez)
```

## 🔐 Autenticación con JWT

Todos los endpoints `/api/...` excepto `/api/auth/**`, Swagger y la documentación OpenAPI requieren un token JWT en la cabecera `Authorization: Bearer <token>`. Si falta o es inválido la API responde **401**.

### Endpoints públicos
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/register` | Crea un usuario nuevo y devuelve un token. |
| POST | `/api/auth/login` | Autentica al usuario y devuelve un token. |

Ejemplo:
```bash
# 1) Registro
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"david","password":"secreto123"}'
# -> { "token": "eyJ...", "username": "david", "expiresIn": 3600 }

# 2) Llamada autenticada
curl -H "Authorization: Bearer eyJ..." http://localhost:8080/api/productos
```

### Postman automatiza el token
La colección **"Inventario API - COMPLETA (Con Filtros Combinados)"** incluye:
- Un folder **"0. Autenticación"** con `register` y `login`.
- Variables de colección: `authUsername`, `authPassword`, `authToken`, `authTokenExpiresAt`, `baseUrl`.
- Un pre-request script global que, antes de cada request, comprueba si el token sigue vigente. Si no lo está, hace login automático y guarda el token en `authToken`. No tienes que pasar el token a mano nunca.
- Auth Bearer global con `{{authToken}}`, así todas las requests heredan la cabecera sin tener que añadirla manualmente.

Si quieres usar otras credenciales solo tienes que cambiar las variables `authUsername` / `authPassword` en la pestaña *Variables* de la colección.

## 📚 Endpoints CRUD (6 entidades)

Cada entidad tiene operaciones CRUD completas:

### Productos
- `POST /api/productos` - Crear producto
- `GET /api/productos` - Listar productos (con filtrado opcional)
- `GET /api/productos?nombre=Tornillo` - Filtrar por nombre
- `GET /api/productos?precioVenta=1.0` - Filtrar por precio
- `GET /api/productos/{id}` - Obtener producto específico
- `PUT /api/productos/{id}` - Actualizar producto
- `DELETE /api/productos/{id}` - Eliminar producto (soft delete)

### Almacenes, Proveedores, Clientes, Compras, Ventas
- Misma estructura CRUD que Productos
- Cada uno con filtrado por hasta 3 campos

## 🎯 Características implementadas

### ✅ Modelo de datos
- 6 Entidades JPA con 6+ atributos cada una
- Validaciones (@Positive, @Email, @Column(nullable=false))
- Relaciones @ManyToOne (Compra y Venta con Proveedor/Cliente, Producto, Almacén)

### ✅ Capa de acceso a datos
- 6 Repositories (CrudRepository)
- Soft delete implementado (campo `activo`)
- Tests de integración con BD

### ✅ Lógica de negocio
- 6 Services con validaciones de negocio
- Métodos CRUD: crear(), buscarPorId(), buscarTodos(), actualizar(), eliminar()
- Filtrado avanzado (hasta 3 campos por entidad)

### ✅ API REST
- 6 Controllers con endpoints CRUD
- Manejo de errores HTTP (201, 200, 400, 404, 500)
- ErrorResponse para errores consistentes
- OpenAPI 3.0 (Swagger UI)

### ✅ Testing
- **106+ tests pasando**
    - 6 Repository tests (integración con BD)
    - 6 Service tests (lógica de negocio)
    - 6 Controller tests (endpoints HTTP)
    - 3 WireMock tests (simulación de APIs externas)

### ✅ Documentación
- OpenAPI 3.0 con Swagger UI: `http://localhost:8080/swagger-ui.html`
- Colección Postman: `inventario-api.postman_collection.json`
- README.md con instrucciones completas
- .gitignore configurado para Java/Gradle

### ✅ Control de versiones
- GitHub con commits organizados
- Ramas: main (producción), develop (desarrollo)
- Issues creados para seguimiento

## 🧪 Tests

```bash
./gradlew test
```

**Resultado:**
```
BUILD SUCCESSFUL - 106+ tests pasando
- Repository Tests: 6 ✅
- Service Tests: 30+ ✅
- Controller Tests: 30+ ✅
- WireMock Tests: 3 ✅
```

## 📖 Documentación API

### OpenAPI 3.0 / Swagger UI
```bash
./gradlew bootRun
# Abre: http://localhost:8080/swagger-ui.html
```

Documenta todos los endpoints con:
- Descripción de cada operación
- Parámetros requeridos/opcionales
- Ejemplos de request/response
- Códigos HTTP esperados

### Colección Postman
1. Descarga: `inventario-api.postman_collection.json`
2. Abre Postman → Import → Upload Files
3. Importa la colección
4. Prueba los 36 endpoints (6 por entidad)

**Estructura:**
- 6 carpetas (Productos, Almacenes, Proveedores, Clientes, Compras, Ventas)
- 36 requests (POST, GET, GET filtrado, PUT, DELETE por entidad)
- Ejemplos de body para cada operación
- URLs preconfiguradas para localhost:8080

## 🔧 Filtrado (hasta 3 campos por entidad)

### Ejemplos:
```
GET /api/productos?nombre=Tornillo
GET /api/productos?precioVenta=1.0
GET /api/productos?nombre=Tornillo&precioVenta=1.0

GET /api/almacenes?ubicacion=Barcelona&capacidadMaxima=10000
GET /api/proveedores?email=test&diasEntrega=5
GET /api/clientes?ciudad=Madrid
GET /api/compras?estado=RECIBIDA&cantidad=100
GET /api/ventas?estado=ENTREGADA
```

## 📊 Estructura del Proyecto

```
inventario-api-AD/
├── src/
│   ├── main/java/com/example/inventarioapiad/
│   │   ├── entity/           (6 entidades JPA)
│   │   ├── repository/       (6 repositories)
│   │   ├── service/          (6 services)
│   │   └── controller/       (6 controllers REST)
│   └── test/java/
│       └── (106+ tests)
├── build.gradle              (Spring Boot 3.2.2, Gradle 8.5)
├── README.md                 (esta documentación)
├── .gitignore               (Java/Gradle)
└── inventario-api.postman_collection.json
```

## 🛠️ Stack Tecnológico

- **Framework:** Spring Boot 3.2.2
- **Build:** Gradle 8.5
- **BD:** MariaDB 12.1
- **JDK:** Java 21
- **Testing:** JUnit 5, Mockito, MockMvc, WireMock
- **Documentación:** SpringDoc OpenAPI 3.0
- **Herramientas:** Postman, GitHub, Git Flow

## 🚀 Próximas funcionalidades (Segunda entrega)

- [ ] PATCH para actualización parcial de entidades
- [ ] 3 nuevos endpoints con DTOs
- [ ] Seguridad JWT
- [ ] Consultas JPQL y SQL nativas
- [ ] Manejo de archivos
- [ ] Logging avanzado
- [ ] 2 clases adicionales al modelo
- [ ] Colección Postman parametrizada

## 👨‍💻 Autor

DAVID GARCIA SESMA

## 📝 Licencia

Proyecto académico - Acceso a Datos (DAM)