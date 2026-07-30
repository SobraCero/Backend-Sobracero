# Entrega Final - Arquitectura de Software: Sobra Cero

## 1. Requerimientos del Sistema y Casos de Uso (MVP)
El MVP de **Sobra Cero** tiene como objetivo facilitar la donación de alimentos próximos a vencer o en sobre-stock desde Donantes (supermercados, restaurantes) hacia ONGs. 

**Casos de Uso principales:**
- **Registro y Autenticación:** Registro de usuarios como Donante u ONG.
- **Gestión de Donaciones (Donante):** Publicar alimentos disponibles para donar (con fotos, descripción y fecha de caducidad).
- **Reserva de Donaciones (ONG):** Explorar donaciones disponibles en el mapa o lista y reservarlas para su retiro.
- **Gestión de Estado:** Cambiar estados de las donaciones (Disponible, Reservado, Retirado).
- **Panel de Administración:** Gestión centralizada de usuarios y métricas por parte del administrador.



## 3. Estilo Arquitectónico
El sistema utiliza una arquitectura **Cliente-Servidor (Separación Frontend/Backend)** basada en el patrón arquitectónico de **Micro-núcleo / Monolito Modular**, acompañado de un estilo de comunicación **RESTful**.
- **Justificación:** Se eligió un backend monolítico en Spring Boot porque permite un desarrollo ágil y centralizado de la lógica del negocio para un equipo pequeño, manteniendo la capacidad de escalar en un futuro. La separación total del frontend en Vue.js permite el despliegue independiente estático, mejorando el rendimiento y la seguridad del sistema al dejar la lógica central aislada en el servidor.

## 4. Aspectos Técnicos Relevantes
- **Frontend:** Vue 3 con Composition API, Vite, TailwindCSS (para diseño rápido y dinámico), Leaflet (para mapas).
- **Backend:** Java 17, Spring Boot 3, Spring Security (con JWT), Spring Data JPA, MinIO SDK (Almacenamiento S3).
- **Persistencia:** PostgreSQL.
- **Despliegue y Contenedores:** Docker y Docker Compose para orquestar la DB, el backend y el almacenamiento local. Despliegue con Caddy/Nginx como Proxy Inverso.
- **Pipelines:** Gitlab CI configurado (`.gitlab-ci.yml`) para automatizar el build o despliegue.
- **Seguridad Básica:** Uso de JWT para la protección de rutas, contraseñas encriptadas con BCrypt y reglas de CORS en el API.

## 5. Pruebas de Software
Las pruebas han sido configuradas y añadidas al proyecto:
- **Backend:** Se configuró `spring-boot-starter-test`. Se han desarrollado pruebas unitarias usando MockMvc y JUnit 5 (ej: `FileControllerTest`) que cubren el componente de subida de archivos, falseando el servicio (`FileService`) para asegurar respuestas exitosas.
- **Frontend:** Se configuró `vitest` junto a `@vue/test-utils`. Se probaron componentes comunes de la interfaz (ej: `GenericModal.spec.ts`) para validar la renderización condicional, las ranuras (slots) y la emisión correcta de eventos.

## 6. Instrucciones de Instalación y Despliegue

### Backend Local
1. Asegurar tener Java 17 y Docker instalados.
2. Levantar servicios: `docker-compose up -d db minio`
3. Ejecutar backend: `./mvnw spring-boot:run`
4. Pruebas: `./mvnw test`

### Frontend Local
1. Instalar dependencias: `npm install`
2. Levantar entorno: `npm run dev`
3. Pruebas: `npm install -D vitest @vue/test-utils jsdom && npx vitest run`

### Despliegue (VPS / EC2 AWS)
- **Producción (AWS EC2):** 
  - Subir código al servidor o hacer clone.
  - Generar builds (`npm run build` para frontend, `./mvnw clean package` para backend).
  - Levantar contenedores mediante `docker-compose up -d --build`.
  - Configurar las variables de entorno (`.env`) apuntando a los host correspondientes.
  - El proxy inverso (Nginx / Caddy) enrruta el tráfico al puerto 80/443.

## 7. Aspectos Destacables
- Implementación de un mapa interactivo geolocalizado en el frontend usando `Leaflet`.
- Integración de sistema de almacenamiento de objetos estandarizado S3 (MinIO) para un manejo eficiente de imágenes, en lugar de guardar blobs en la DB.
- Interfaz gráfica premium, fluida, animada y completamente responsive.

## 8. Limitaciones y Mejoras Futuras
- **Limitaciones actuales:** AL ser un MVP, no contamos con balanceadores de carga.
- **Mejoras futuras (Deuda Técnica):** Aumentar la cobertura de pruebas de software, implementar websockets para notificaciones en tiempo real cuando se publica una donación cerca, y configurar balanceo de carga para el backend.

