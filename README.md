# Inventario API - Spring Boot

API REST para gestión de inventario con Spring Boot 3.2.2

## 📋 Requisitos

- Java 21
- Gradle 8.5
- MariaDB 12.1

## 🚀 Cómo ejecutar

### 1. Configurar Base de Datos
```bash
mysql -u root -p
CREATE DATABASE inventario_db;
```

### 2. Clonar el proyecto
```bash
git clone https://github.com/TU_USUARIO/inventario-api-AD.git
cd inventario-api-AD
```

### 3. Ejecutar tests
```bash
./gradlew test
```

### 4. Ejecutar la aplicación
```bash
./gradlew bootRun
```

La API estará disponible en: `http://localhost:8080`

## 📚 Endpoints

- `POST /api/productos` - Crear producto
- `GET /api/productos/{id}` - Obtener producto
- `GET /api/productos` - Listar productos
- `PUT /api/productos/{id}` - Actualizar producto
- `DELETE /api/productos/{id}` - Eliminar producto

(Similar para almacenes, proveedores, clientes, compras, ventas)

## 🧪 Tests
```bash
./gradlew test
```

- Repository Tests: 6 tests ✅
- Service Tests: 30+ tests ✅
- Controller Tests: 30+ tests ✅

## 📖 Documentación

OpenAPI 3.0: `/swagger-ui.html` (próximamente)

## 👨‍💻 Autor

DAVID GARCIA SESMA