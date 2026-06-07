# ========================================================================
# Dockerfile multi-stage para la API de Inventario
# ========================================================================
# Stage 1 (build): compila el proyecto con el Gradle wrapper dentro del
#                  contenedor. Usa la misma imagen base (Temurin JDK 21)
#                  que el runtime, multi-arch (amd64 + arm64).
# Stage 2 (runtime): imagen mínima con sólo el JRE y el JAR resultante.
# ========================================================================

# ---------- Stage 1: build ----------
# Imagen Debian (glibc) en lugar de Alpine (musl): las librerías nativas
# de Gradle para file-watching crashean con SIGSEGV en Alpine + ARM64.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copia el wrapper y los ficheros de configuración primero para cachear
# la descarga de Gradle y las dependencias
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Copia el código fuente y construye el JAR (sin tests: los tests requieren
# MariaDB y WireMock arrancados, se ejecutan en el host o en CI)
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario no root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
