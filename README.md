# 🛠️ VetHD - Backend Service

![Java](https://img.shields.io/badge/Java-23-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![SpringBoot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-PostgreSQL-3ECF8E?style=flat-square&logo=supabase&logoColor=white)
![Render](https://img.shields.io/badge/Render-Cloud_Hosting-46E3B7?style=flat-square&logo=render&logoColor=white)
![Jira](https://img.shields.io/badge/Jira-HGV_Project-0052CC?style=flat-square&logo=jira&logoColor=white)

> [!NOTE]
> **Proyecto Universitario**: Backend REST API y Base de Datos para Sistema Clínico Veterinario  
> **Curso**: Herramientas de Desarrollo (HD) — Grupo 5  
> **Proyecto Jira**: [HD-Grupo5-Veterinaria (Key: `HGV`)](https://edunavdo.atlassian.net/jira/software/projects/HGV/boards/34/backlog)

---

## 👥 1. Integrantes del Equipo

|  #  | Apellidos y Nombres                   | Usuario de GitHub                                    | Rol           |
| :-: | :------------------------------------ | :--------------------------------------------------- | :------------ |
|  1  | **Navarro Domínguez, Rommel Eduardo** | [@Edudev2004](https://github.com/Edudev2004)         | Desarrollador |
|  2  | **Alaya Vargas, Luis Fernando**       | [@LAVIS7](https://github.com/LAVIS7)                 | Desarrollador |
|  3  | **Alvarado Chávez, Romina Liz**       | [@RominaAlvarado](https://github.com/RominaAlvarado) | Desarrollador |
|  4  | **Chugnas Lupuchi, Diego Augusto**    | [@August-CH57](https://github.com/August-CH57)       | Desarrollador |
|  5  | **Cuipal Jara, José Martín**          | [@Josema13Cj](https://github.com/Josema13Cj)         | Desarrollador |
|  6  | **Dextre Cabrera, Enrique Eduardo**   | [@Enrique_Dc](https://github.com/Enrique_Dc)         | Desarrollador |
|  7  | **Medina Niño, Fabrizio Adrián**      | [@FabrizioMn](https://github.com/FabrizioMn)         | Desarrollador |
|  8  | **Olivares Quispetera, José Carlos**  | [@JoseOlivares19](https://github.com/JoseOlivares19) | Desarrollador |

---

## 📖 2. Descripción General

Este repositorio contiene la arquitectura backend REST API construida en **Java con Spring Boot 3**, conectada a **Supabase (PostgreSQL)** y preparada para despliegue continuo en **Render**.

El diseño del software sigue la estricta **Arquitectura en Capas (Layered Architecture)** para garantizar la separación de responsabilidades, mantenibilidad y facilidades de prueba.

---

## 🛠️ 3. Tecnologías y Nube

| Categoría | Tecnología | Detalle |
| :--- | :--- | :--- |
| **Lenguaje** | Java 23 | Java SE / OpenJDK 23 |
| **Framework REST API** | Spring Boot 3 | Spring Web, Spring Data JPA, Spring Security, Validation |
| **Base de Datos** | Supabase (PostgreSQL) | PostgreSQL administrado con esquemas relacionales |
| **Arquitectura** | Capas (Layered) | `controller`, `service`, `repository`, `entity`, `dto`, `config`, `exception` |
| **Despliegue Cloud** | Render | Hosting continuo para servicios backend Web Service |
| **Gestión de Proyecto** | Jira Cloud | Metodología Ágil / Sprints & Épicas (`HGV`) |

---

## 🚀 4. Planificación de Sprints (Jira Roadmap)

### Sprint 2 - Backend REST API (`HGV-42` a `HGV-67`)
- **Endpoints de Autenticación (HGV-42 a HGV-46)**: Registro de propietarios, login y generación de JWT, logout, recuperación de contraseña y filtro Spring Security RBAC.
- **Roles y Permisos (HGV-47 & HGV-48)**: Endpoints para la matriz de permisos y perfiles de usuario (GET/PUT).
- **Gestión de Mascotas (HGV-49 & HGV-50)**: Endpoints para registro, listado y edición de mascotas (pacientes).
- **Citas y Especialidades (HGV-51 a HGV-57)**: Endpoints de especialidades, disponibilidad de veterinarios, agendamiento, modificación y cancelación de citas.
- **Historial y Consultas (HGV-58 a HGV-63)**: Endpoints para historial clínico, agenda diaria, registro de diagnósticos/tratamientos, estados de citas y valoraciones.
- **Administración (HGV-64 a HGV-66)**: Endpoints de gestión de veterinarios, especialidades y reportes/estadísticas.
- **Infraestructura de Datos (HGV-67 / DB-01)**: Diseño e inicialización de la base de datos PostgreSQL en Supabase.

### Sprint 3 - Integración & CORS (`HGV-68` a `HGV-76`)
- **INT-01 a INT-08**: Integración de todos los módulos REST con la aplicación Frontend.
- **INT-09 (HGV-76)**: Configuración CORS, variables de entorno de producción y manejo centralizado de excepciones.

### Sprint 4 - Pruebas QA & Despliegue en Render (`HGV-77` a `HGV-84`)
- **QA-01 (HGV-77)**: Pruebas unitarias backend con JUnit/Mockito (cobertura >= 70%).
- **QA-03 (HGV-79)**: Pruebas de seguridad, control de acceso y tokens JWT.
- **DEPLOY-02 (HGV-83)**: Build y despliegue automatizado del servicio backend en Render.
- **DEPLOY-03 (HGV-84)**: Verificación en entorno de producción y entrega final.

---

## 🎯 5. Épicas del Proyecto (`HGV`)

1. **EPIC-01 (`HGV-1`)**: Autenticación y Seguridad (Auth, JWT, RBAC)
2. **EPIC-02 (`HGV-2`)**: Gestión de Perfil y Mascotas (Clientes y Pacientes)
3. **EPIC-03 (`HGV-3`)**: Agendamiento y Citas Médicas
4. **EPIC-04 (`HGV-4`)**: Portal del Propietario (Citas, Historial Clínico, Recordatorios)
5. **EPIC-05 (`HGV-5`)**: Panel Veterinario (Agenda Diaria, Diagnósticos, Atenciones)
6. **EPIC-06 (`HGV-6`)**: Panel de Administración (Gestión General, Roles, Estadísticas)
7. **EPIC-07 (`HGV-7`)**: Notificaciones y Valoraciones del Servicio

---

## 🧱 6. Arquitectura en Capas (Layered Architecture)

```text
src/main/java/com/veterinariahd/backend/
├── config/                 # Configuraciones globales (CORS, Security, Supabase DB connection)
├── controller/             # Capa de Presentación (Controladores REST, Endpoints HTTP)
├── dto/                    # Data Transfer Objects (Request/Response DTOs y Mappers)
├── entity/                 # Entidades JPA mapeadas a las tablas de Supabase Postgres
├── exception/              # Manejo centralizado de excepciones globales (@ControllerAdvice)
├── repository/             # Capa de Acceso a Datos (Interfaces Spring Data JPA)
├── service/                # Contratos e Interfaces de Servicios de Negocio
│   └── impl/               # Implementaciones de lógica de negocio (@Service)
└── BackendApplication.java # Clase principal Spring Boot
```

---

## ⚙️ 7. Ejecución Local

1. **Clonar el repositorio**:
   ```bash
   git clone https://github.com/Edudev2004/hd-veterinaria-backend.git
   cd hd-veterinaria-backend
   ```

2. **Configurar variables de entorno (`application.properties`)**:
   ```properties
   spring.datasource.url=jdbc:postgresql://<SUPABASE_HOST>:5432/postgres
   spring.datasource.username=postgres
   spring.datasource.password=<SUPABASE_PASSWORD>
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Ejecutar la aplicación**:
   ```bash
   ./mvnw spring-boot:run
   ```

---

## 🌿 8. Flujo de Trabajo (GitFlow)

- `main`: Rama de producción lista para despliegue continuo en Render.
- `develop`: Rama de integración continua backend.

---

## 🔐 9. Estrategia de Autenticación, JWT y Cierre de Sesión (Logout)

### Generación de Tokens
- Al invocar `POST /api/auth/register` o `POST /api/auth/login`, el backend valida las credenciales contra la base de datos (utilizando **BCrypt**) y genera un token JWT firmado mediante algoritmo HMAC-SHA (mínimo 256 bits).
- El JWT incluye en sus claims: `userId`, `correo`/`email`, `rol`/`role` y tiempo de expiración (24 horas).

### Invalidación de Token (Logout Blacklist)
- **Endpoint:** `POST /api/auth/logout`.
- **Mecanismo:** El token enviado en el header `Authorization: Bearer <token>` (o en el cuerpo `{ "token": "..." }`) es registrado de manera inmediata en la lista negra (`blacklistedTokens`).
- **Comportamiento:**
  - Cualquier petición subsiguiente con un token invalidado es rechazada con código **HTTP 401 Unauthorized**.
  - Si se intenta llamar al endpoint `/logout` con un token ya invalidado o malformado, el sistema responde **HTTP 401**.
  - Los tokens en lista negra se limpian automáticamente conforme expira su tiempo de validez natural (`pruneExpiredBlacklistedTokens`), evitando sobrecarga de memoria.

### Filtro Spring Security y Autorización por Rol (RBAC)
- **Filtro (`JwtAuthenticationFilter`):** Intercepta cada solicitud HTTP inspeccionando el header `Authorization: Bearer <token>`.
- **Validación Automática:** Comprueba firma criptográfica HMAC-SHA, expiración y verifica contra la lista negra en memoria antes de permitir el acceso.
- **Roles y Permisos:**
  - `PROPIETARIO` (compatible con `ROLE_OWNER`): Gestión de citas propias, mascotas y valoraciones (`POST /api/citas/*/valoracion`).
  - `VETERINARIO` (compatible con `ROLE_VET`): Consulta de agenda médica (`GET /api/veterinarios/me/agenda`), atención de citas (`PATCH /api/citas/*/atendida`) y emisión de diagnósticos (`POST /api/citas/*/consulta`).
  - `ADMIN`: Acceso a operaciones privilegiadas (`/api/admin/**`) y supervisión global del sistema.
- **Manejo Estandarizado de Respuestas:**
  - **HTTP 401 Unauthorized (`JwtAuthenticationEntryPoint`):** Retornado en formato JSON cuando falta el token, es inválido, está expirado o revocado.
  - **HTTP 403 Forbidden (`JwtAccessDeniedHandler`):** Retornado en formato JSON cuando el rol autenticado no posee los permisos requeridos por el recurso.
- **Endpoints Públicos (sin requerir token):**
  - `/api/auth/**` (registro, login, logout, recuperación y reset de contraseña).
  - `/api/especialidades/**` (catálogo activo de especialidades).
  - `/api/veterinarios/**` (listado público de veterinarios, disponibilidad y valoraciones públicas, excluyendo `/api/veterinarios/me/**`).

### Recuperación y Restablecimiento de Contraseña (US-05)
- **Solicitud de Recuperación:**
  - **Endpoint:** `POST /api/auth/recuperar-password`.
  - **Payload:** `{ "correo": "usuario@ejemplo.com" }` (o `{ "email": "..." }`).
  - **Comportamiento:** Si el correo existe en la base de datos, genera un token temporal con vigencia estricta de **30 minutos** persistido en la tabla `password_reset_tokens`.
  - **Simulación de Correo (Log):** Conforme al criterio de aceptación, el sistema emite el enlace en los logs:
    `[PASSWORD_RESET] Correo simulado a usuario@ejemplo.com: Para restablecer su contraseña use el enlace: https://vethd.com/reset-password?token=...`
  - **Respuesta:** HTTP 200 OK (`{"message": "Se ha enviado un enlace de recuperación a su correo electrónico", "token": "..."}`).
- **Restablecimiento de Contraseña:**
  - **Endpoint:** `POST /api/auth/reset-password`.
  - **Payload:** `{ "token": "...", "nuevaPassword": "NuevaPassword123!" }`.
  - **Validaciones:**
    - Token inexistente o vacío: HTTP 400 Bad Request.
    - Token expirado (> 30 minutos): HTTP 400 Bad Request.
    - Token ya utilizado: HTTP 400 Bad Request.
    - Contraseña con menos de 8 caracteres: HTTP 400 Bad Request.
  - **Resultado:** Actualiza el hash de la contraseña con BCrypt, invalida el token (`used = true`) y permite el inicio de sesión inmediato con la nueva clave.

---

## 📄 10. Licencia

Desarrollado para el curso de **Herramientas de Desarrollo (HD)** &copy; 2026.