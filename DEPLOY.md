# Despliegue en AWS Academy (Learner Lab)

Esta guía explica cómo desplegar la Inventario API en una instancia EC2 dentro del Learner Lab de AWS Academy, cubriendo el **Obligatorio #3 de la 2ª evaluación** ("Despliega la API en AWS").

**Lo que vas a tener al final:**
- Una EC2 `t3.small` con Amazon Linux 2023 corriendo Docker.
- El stack `inventario-api` + `mariadb` arrancado con Docker Compose (perfil `docker`, mismo que en local).
- La API accesible públicamente en `http://<ip-publica>:8080`.

> ℹ️ **Apiman NO se despliega en AWS.** El Apiman necesita ~8 GB de RAM y 7 servicios; las instancias de Learner Lab son pequeñas (1–2 GB). El despliegue de Apiman en AWS es un extra opcional que se deja fuera del alcance de esta entrega.

---

## 0. Antes de empezar

1. **Inicia el Learner Lab**:
   - Entra a tu curso de AWS Academy.
   - Botón **Start Lab** (espera al círculo verde).
   - Botón **AWS** (te abre la consola en una pestaña nueva).
2. Comprueba que estás en la región **`N. Virginia (us-east-1)`** (esquina superior derecha de la consola).

---

## 1. Crear el Key Pair (par de claves SSH)

Lo necesitas para entrar por SSH a la EC2 más tarde.

1. Consola AWS → busca **EC2** → menú izquierdo: **Network & Security → Key Pairs**.
2. Botón **Create key pair**:
   - **Name**: `inventario-key`
   - **Key pair type**: `RSA`
   - **Private key file format**: `.pem` (Mac/Linux) o `.ppk` (Windows + PuTTY)
3. **Create key pair** → se descarga `inventario-key.pem`.
4. En tu Mac, **dale permisos restringidos** (si no, SSH se quejará):
   ```bash
   chmod 400 ~/Downloads/inventario-key.pem
   ```

---

## 2. Crear el Security Group

El Security Group es el "firewall" de la EC2: define qué puertos están abiertos y desde qué IPs.

1. EC2 → **Network & Security → Security Groups → Create security group**.
2. Configura:
   - **Name**: `inventario-sg`
   - **Description**: `Inventario API: SSH 22 + HTTP 8080`
   - **VPC**: la que aparece por defecto (no la toques).
3. **Inbound rules → Add rule** dos veces:

   | Type | Protocol | Port | Source | Descripción |
   |---|---|---|---|---|
   | SSH | TCP | 22 | **My IP** | SSH solo desde tu IP actual |
   | Custom TCP | TCP | 8080 | Anywhere-IPv4 (`0.0.0.0/0`) | API accesible públicamente |

4. **Outbound rules**: deja el "All traffic" por defecto.
5. **Create security group**.

---

## 3. Lanzar la EC2

1. EC2 → **Instances → Launch instances**.
2. Configuración:

   | Campo | Valor |
   |---|---|
   | **Name** | `inventario-api` |
   | **AMI** | `Amazon Linux 2023 AMI` (la primera que aparece, marcada *Free tier eligible*) |
   | **Instance type** | `t3.small` |
   | **Key pair** | `inventario-key` (el del paso 1) |
   | **Network settings** → **Firewall** | Select existing → `inventario-sg` |
   | **Storage** | 8 GiB gp3 (por defecto, suficiente) |

3. **Expandir "Advanced details"** (al final del formulario). Busca el campo **User data** y **pega el contenido completo del fichero [`deploy/user-data.sh`](deploy/user-data.sh)** del repo. Ese script se ejecuta como root al primer boot e instala Docker + arranca todo.

   > ⚠️ Pega el script tal cual, sin modificar. Genera credenciales aleatorias en cada despliegue.

4. **Launch instance**.

---

## 4. Esperar al primer boot

La EC2 arranca rápido (~30 s), pero el `user-data.sh` tarda **5–8 min** en compilar la imagen Docker y levantar el stack.

Mientras esperas:
1. En la lista de instancias, anota la **Public IPv4 address** de tu EC2.
2. Verifica el progreso vía SSH:
   ```bash
   ssh -i ~/Downloads/inventario-key.pem ec2-user@<ip-publica>
   sudo tail -f /var/log/cloud-init-output.log
   # Cuando veas "Despliegue completado", está listo.
   ```

---

## 5. Comprobar que la API responde

Desde tu terminal, sin SSH:

```bash
EC2_IP=<ip-publica-de-tu-ec2>

# Sin token: debe devolver 401
curl -s -o /dev/null -w "HTTP %{http_code}\n" \
  http://${EC2_IP}:8080/api/productos
# → HTTP 401

# Register: debe devolver 200 con un token
curl -X POST http://${EC2_IP}:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"david","password":"secreto123"}'
# → {"token":"eyJ...","username":"david","expiresIn":3600}

# Llamada autenticada
TOKEN=<pega-el-token>
curl -H "Authorization: Bearer $TOKEN" \
     http://${EC2_IP}:8080/api/productos
# → [] HTTP 200
```

---

## 6. Postman contra la EC2

La colección Postman principal ya usa la variable `{{baseUrl}}`. Para apuntarla a AWS:

1. Abre la colección **"Inventario API - COMPLETA (Con Filtros Combinados)"**.
2. Pestaña **Variables**.
3. Cambia `baseUrl` de `http://localhost:8080` a `http://<ip-publica>:8080`.
4. Las requests funcionan exactamente igual: el pre-request script renueva el JWT contra la EC2.

---

## 7. Apagar la EC2 al terminar

Las instancias en Learner Lab **se mantienen** entre sesiones (no las borra el lab al cerrarse), pero **siguen consumiendo crédito**. Cuando hayas acabado el día:

- **Stop instance** (acción del menú → Instance state → Stop). La paras pero la conservas; al volver a hacer Start sigue todo igual.
- **Terminate instance** sólo cuando ya hayas presentado el trabajo. Termina la instancia y libera el almacenamiento.

---

## Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Connection timed out` | Security group sin puerto 8080 abierto | Revisa el paso 2 |
| SSH dice `Permission denied (publickey)` | Permisos del `.pem` mal | `chmod 400 ~/Downloads/inventario-key.pem` |
| `Connection refused` en puerto 8080 | La API aún no arrancó | Espera más, revisa `cloud-init-output.log` |
| `curl: HTTP/1.1 500` | Probablemente Spring Boot petó | `docker logs inventario-api` en la EC2 |
| `Cannot connect to the Docker daemon` | El usuario ec2-user aún no estaba en el grupo `docker` | `exit` y vuelve a hacer SSH, o `sudo` para el comando |
| Build falla por OOM (out of memory) | t3.small va justa | Stop instance → Change instance type → `t3.medium` (4 GB RAM) |

## ¿Cómo verifico que el script user-data hizo su trabajo?

```bash
ssh -i ~/Downloads/inventario-key.pem ec2-user@<ip-publica>

# 1) El stack está vivo
docker ps
# Debe haber 2 contenedores: inventario-api e inventario-mariadb

# 2) Logs de la API
docker logs --tail 50 inventario-api

# 3) Log completo del primer boot
sudo cat /var/log/cloud-init-output.log
```
