#!/bin/bash
# =============================================================================
# Script de "User data" para EC2 (Amazon Linux 2023)
# =============================================================================
# Se ejecuta como root en el PRIMER arranque de la instancia. Lo único que
# tienes que hacer es pegarlo en el campo "User data" al lanzar la EC2.
#
# Qué hace:
#   1. Instala Docker y Docker Compose v2.
#   2. Clona el repo de GitHub.
#   3. Genera un .env con credenciales aleatorias (la BD y la clave JWT
#      no son las mismas que en local).
#   4. Arranca el stack con `docker compose up -d --build`.
#
# Tarda 5–8 minutos. Para ver el progreso:
#   ssh ec2-user@<ip> "sudo tail -f /var/log/cloud-init-output.log"
# =============================================================================

set -euxo pipefail

# Mostramos cada paso con la marca de tiempo en el log
log() { echo "[$(date '+%H:%M:%S')] $*"; }

# ---- 1. Sistema y dependencias ----------------------------------------------
log "Actualizando paquetes del sistema..."
dnf update -y

log "Instalando Docker, Git y herramientas básicas..."
dnf install -y docker git
systemctl enable --now docker

# Permitir a ec2-user usar docker sin sudo (para cuando entremos por SSH)
usermod -aG docker ec2-user

# Docker Compose v2 como plugin de la CLI
log "Instalando Docker Compose v2..."
DOCKER_COMPOSE_VERSION="v2.29.7"
mkdir -p /usr/local/lib/docker/cli-plugins
curl -sSL "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-linux-x86_64" \
    -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# ---- 2. Código de la API ----------------------------------------------------
log "Clonando el repositorio..."
APP_DIR=/opt/inventario-api
git clone https://github.com/davidgarcia11/inventario-api-AD.git "$APP_DIR"
cd "$APP_DIR"

# ---- 3. Secretos -----------------------------------------------------------
# Generamos credenciales aleatorias para esta instancia. Nada de valores
# por defecto: cada despliegue tiene sus propias claves.
log "Generando .env con secretos aleatorios..."
DB_ROOT_PASSWORD=$(openssl rand -hex 16)
DB_PASSWORD=$(openssl rand -hex 16)
JWT_SECRET=$(openssl rand -base64 32)

cat > .env <<EOF
# Generado automáticamente por user-data.sh en el primer boot
DB_ROOT_PASSWORD=${DB_ROOT_PASSWORD}
DB_NAME=inventario_db
DB_USERNAME=inventario
DB_PASSWORD=${DB_PASSWORD}
JWT_SECRET=${JWT_SECRET}
EOF
chmod 600 .env

# ---- 4. Arrancar el stack --------------------------------------------------
log "Levantando el stack con Docker Compose (compila la imagen, tarda unos minutos)..."
docker compose up -d --build

log "Despliegue completado. La API estará disponible en http://<ip-publica>:8080 en cuanto Spring Boot termine de arrancar."
log "Para verificar: curl http://localhost:8080/api/auth/login -X POST -H 'Content-Type: application/json' -d '{}' (espera 401)"
