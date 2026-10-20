# 🌱 Sobra Cero — Backend

Backend de la plataforma **Sobra Cero**, una aplicación que conecta **donantes** (supermercados, restaurantes, comercios) con **ONGs** para facilitar la donación de alimentos próximos a vencer o en sobre-stock, reduciendo el desperdicio alimentario.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.2 |
| Seguridad | Spring Security + JWT (JJWT 0.11.5) |
| Persistencia | Spring Data JPA + PostgreSQL 15 |
| Almacenamiento | MinIO (compatible con S3) |
| Email | Spring Mail (SMTP Gmail) |
| Templates | Thymeleaf |
| Build | Maven (Maven Wrapper) |
| Contenedores | Docker + Docker Compose |
| Proxy Inverso | Caddy 2 / Nginx |

---

## 📁 Estructura del Proyecto

```
src/main/java/sobracero/backend/
├── controllers/
│   ├── auth/          # Registro, login, verificación, recuperación de contraseña
│   ├── donante/       # Gestión de donaciones (crear, editar, cambiar estado)
│   ├── ong/           # Exploración y reserva de donaciones
│   ├── admin/         # Panel de administración de usuarios y donaciones
│   └── FileController # Subida de imágenes/documentos a MinIO
├── models/            # Entidades JPA (Usuario, Donacion, Reserva, HistorialEstado…)
├── dtos/              # Objetos de transferencia de datos
├── mappers/           # Conversión entre entidades y DTOs
├── repositories/      # Interfaces JPA Repository
├── services/          # Interfaces de servicios
├── servicesImpl/      # Implementaciones de servicios
├── security/          # Filtros JWT, configuración de Spring Security
└── config/            # Configuración de CORS, MinIO, Mail
```

---

## 🔑 Funcionalidades Principales

- **Autenticación JWT** — Registro, login y refresh de tokens. Contraseñas encriptadas con BCrypt.
- **Verificación de cuenta por email** — Envío de email con enlace de verificación vía SMTP.
- **Recuperación de contraseña** — Flujo completo de reset por email con token temporal.
- **Gestión de donaciones (Donante)** — Publicar alimentos con fotos, descripción y fecha de caducidad.
- **Reserva de donaciones (ONG)** — Explorar y reservar donaciones disponibles.
- **Historial de estados** — Trazabilidad completa: `DISPONIBLE → RESERVADO → RETIRADO`.
- **Panel de Administración** — Gestión centralizada de usuarios y donaciones.
- **Almacenamiento de archivos** — Imágenes y documentos gestionados en MinIO (S3-compatible).

---

## 🚀 Cómo correr el proyecto

### Requisitos previos
- Java 17+
- Docker y Docker Compose

### 1. Variables de entorno

Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:

```env
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sobracero_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=tu_password

# MinIO
MINIO_URL=http://localhost:9000
MINIO_ACCESS_NAME=adminminio
MINIO_ACCESS_SECRET=adminminiopassword
MINIO_BUCKET_NAME=imagenes-sobracero
MINIO_BUCKET_DOCS=documentos-privados

# Email (SMTP Gmail)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=tu_correo@gmail.com
SPRING_MAIL_PASSWORD=tu_app_password
```

### 2. Levantar servicios de infraestructura

```bash
docker-compose up -d db-sobracero minio
```

### 3. Ejecutar el backend

```bash
./mvnw spring-boot:run
```

El servidor queda disponible en `http://localhost:8080`.

### 4. Ejecutar pruebas

```bash
./mvnw test
```

---

## 🐳 Despliegue con Docker Compose (Producción)

Levanta todos los servicios (DB, MinIO, backend, frontend y proxy Caddy):

```bash
docker-compose up -d --build
```

El proxy Caddy gestiona automáticamente los certificados TLS y enruta el tráfico en los puertos `80` y `443`.

---

## 🔐 Seguridad

- Autenticación basada en **JWT Bearer tokens**.
- Rutas protegidas por rol: `DONANTE`, `ONG`, `ADMIN`.
- Contraseñas hasheadas con **BCrypt**.
- **CORS** configurado para el dominio del frontend.

---

## 📐 Arquitectura

El sistema sigue un patrón de **Monolito Modular** con separación total del frontend (Vue 3). La comunicación es **RESTful** y el almacenamiento de archivos es delegado a MinIO para no saturar la base de datos con BLOBs.

```
[Frontend Vue 3] ←→ [Caddy Proxy] ←→ [Spring Boot API] ←→ [PostgreSQL]
                                                        ←→ [MinIO (S3)]
                                                        ←→ [SMTP Gmail]
```
