# ms-digitalfix-usuarios

**Proyecto:** DigitalFix - Gestion de ordenes de trabajo para una red de 20 empresas de mantenimiento electrico  
**Componente:** Microservicio  
**Asignatura:** DSY1107 Desarrollo Cloud Native I - Duoc UC  
**Primera entrega (EP1):** 14 de septiembre de 2026

## Descripcion

Usuarios y empresas: alta automatica al primer ingreso, resolucion de empresa y roles, y estado del usuario.

## Tecnologias

Spring Boot 3, Spring Data JPA, Oracle, Flyway

## Integrantes

| Integrante | GitHub |
|---|---|
| Kevin Rojas | @khrojasdev |
| Christopher Perez | @ChrisPerezV |
| Diego Lopez | @DiegoLopez-f |

## Repositorios del proyecto

- [`digitalfix-frontend`](https://github.com/khrojasdev/digitalfix-frontend) - Frontend
- [`ms-digitalfix-bff`](https://github.com/khrojasdev/ms-digitalfix-bff) - BFF / API interna
- [`ms-digitalfix-usuarios`](https://github.com/khrojasdev/ms-digitalfix-usuarios) - Microservicio **(este)**
- [`ms-digitalfix-catalog`](https://github.com/khrojasdev/ms-digitalfix-catalog) - Microservicio
- [`ms-digitalfix-workorders`](https://github.com/khrojasdev/ms-digitalfix-workorders) - Microservicio
- [`ms-digitalfix-notify`](https://github.com/khrojasdev/ms-digitalfix-notify) - Microservicio
- [`ms-digitalfix-report`](https://github.com/khrojasdev/ms-digitalfix-report) - Microservicio
- [`ms-digitalfix-audit`](https://github.com/khrojasdev/ms-digitalfix-audit) - Microservicio
- [`digitalfix-infra`](https://github.com/khrojasdev/digitalfix-infra) - Infraestructura

## Pendiente por definir

El documento del caso pide ademas *un microservicio administrador de RabbitMQ y otro de
Kafka, segun la pauta de cada evaluacion*. Ni la EP1 ni la EP2 los evaluan, asi que no
existen todavia como repositorios. Cuando la pauta de la evaluacion final los exija se
agregaran como `ms-digitalfix-rabbit` y `ms-digitalfix-kafka`. La decision del equipo fue esperar la pauta.

## Tablero

El backlog completo vive en un unico GitHub Project que enlaza los ocho repositorios.
Columnas: Backlog, To Do, In Progress, In Review, Done. Limite de trabajo en curso: 2 tarjetas por persona.

## Como se trabaja aqui

1. Cada tarea del tablero tiene su propia rama, indicada en el cuerpo del issue.
2. `git switch <rama>` - nunca se trabaja directamente sobre `main`.
3. Commits con Conventional Commits: `feat(catalogo): agrega endpoint de servicios`.
4. Pull request hacia `main` con `Closes #<numero del issue>` en la descripcion.
5. Revisa un companero distinto del autor. Recien ahi se hace merge.

Referencias entre repositorios: `khrojasdev/otro-repo#12`.

## Configuracion

Ninguna credencial vive en este repositorio. Todo llega por variables de entorno;
revisa `.env.example` para saber cuales.


# ms-usuarios | DigitalFix

Microservicio principal para la gestión de identidades, autoprovisión de cuentas y control de estado de usuarios de la red de mantención eléctrica DigitalFix. Este servicio está protegido mediante OAuth2/JWT utilizando Microsoft Entra ID (Azure AD) como Proveedor de Identidad (IDaaS).

## Stack Tecnológico
* **Java:** 21
* **Framework:** Spring Boot 3
* **Base de Datos:** Oracle DB (21c)
* **Migraciones:** Flyway
* **Seguridad:** Spring Security (OAuth2 Resource Server) + JWT
* **Contenedores:** Docker & Docker Compose (Base: Ubuntu Jammy para estabilidad de red externa)
  ⚙️ Prerrequisitos
  Para ejecutar este proyecto en tu máquina local, necesitas tener instalado:

Docker Desktop (Asegúrate de que esté en ejecución).

Java 21 y Maven.

Postman (Para pruebas de API y generación de tokens de Azure).

## Guía de Instalación y Ejecución
1. Clonar el repositorio
```
   Bash
   git clone
   cd ms-usuarios
   ```
2. Configurar Variables de Entorno
   Por seguridad, las credenciales no están en el código fuente. Crea un archivo llamado .env en la raíz del proyecto (al mismo nivel que el docker-compose.yml) y agrega las siguientes variables:

Properties
Archivo .env
```
DB_URL=jdbc:oracle:thin:@//host.docker.internal:1521/XEPDB1
DB_USERNAME=tu_usuario_oracle
DB_PASSWORD=tu_password_oracle
(Asegúrate de tener un contenedor de Oracle ejecutándose en el puerto 1521 de tu máquina host).
```
3. Compilar el Proyecto
   Debido a que las credenciales de la base de datos están externalizadas, debes compilar saltando las pruebas para evitar errores de conexión durante el empaquetado:

Bash
```
.\mvnw.cmd clean package -DskipTests
```
4. Levantar el Contenedor
   Levanta el microservicio usando Docker Compose. Flyway se encargará automáticamente de crear las tablas y poblar los datos iniciales (Empresas y usuarios de prueba):

Bash
```
docker compose up -d --build
El servicio estará disponible en http://localhost:8081.
```

## Endpoints Principales

## Configuración en Microsoft Entra ID (Azure AD)

Para que la generación y validación del token JWT funcione correctamente entre Azure, Postman y Spring Security, es **obligatorio** realizar las siguientes configuraciones en el App Registration de tu portal de Azure:

### 1. Registrar Redirect URI para Postman
* Ve a **Authentication**.
* Agrega una plataforma (Web o Single-page application) y añade la siguiente URL de redirección: `https://oauth.pstmn.io/v1/callback`.

### 2. Exponer la API (Scope y Audience)
* Ve a **Expose an API**.
* En la parte superior, haz clic en *Add* junto a **Application ID URI**. Azure generará un valor con el formato `api://`. Guárdalo, ya que esto configura la "audiencia" (`aud`) que Spring Security validará.
* Al configurar Postman, el **Scope** que debes solicitar será siempre `api:///.default`.

### 3. Forzar la Versión 2.0 del Token (¡Crítico!)
Por defecto, Azure puede emitir tokens v1.0, lo que genera un error de `Invalid Issuer` en Spring Boot (ya que espera la v2.0).
* En el menú izquierdo, selecciona **Manifest**.
* Busca la propiedad `"accessTokenAcceptedVersion"`.
* Cambia su valor de `null` a `2`.
* Haz clic en *Save*.

### 4. Creación e Inyección de Roles
Para que el endpoint de autoprovisión capture el perfil del usuario:
* En **App roles**, crea los roles del sistema (`ADMIN`, `SUPERVISOR`, `CLIENTE`, `AUDITOR`). El campo *Value* debe estar en mayúsculas.
* Para asignarlos, ve al inicio de Entra ID -> **Enterprise Applications** -> Busca tu aplicación -> **Users and groups**. Desde ahí podrás asignar los roles a tus cuentas de prueba.


###  Generar Token JWT en Postman (Azure AD)
Para interactuar con la API, necesitas autenticarte y obtener un Access Token válido.

1. En Postman, crea una nueva petición y ve a la pestaña **Authorization**.
2. En el menú desplegable *Type*, selecciona **OAuth 2.0**.
3. En la sección *Configure New Token*, llena los siguientes datos (solicita los IDs al administrador del proyecto si no los tienes):
    * **Grant Type:** Authorization Code
    * **Callback URL:** `https://oauth.pstmn.io/v1/callback` *(Asegúrate de que esta URL esté registrada en el App Registration de Azure en la sección Authentication).*
    * **Auth URL:** `https://login.microsoftonline.com//oauth2/v2.0/authorize`
    * **Access Token URL:** `https://login.microsoftonline.com//oauth2/v2.0/token`
    * **Client ID:** ``
    * **Scope:** `api:///.default`
4. Haz clic en **Get New Access Token**. Se abrirá una ventana del navegador para iniciar sesión con tu cuenta de Microsoft.
5. Tras un inicio de sesión exitoso, Postman capturará el token. Haz clic en **Use Token**.

> **Tip:** Puedes copiar el token generado y pegarlo en [jwt.io](https://jwt.io) para confirmar que el claim `"roles"` incluye el rol correcto (ej. `ADMIN`, `SUPERVISOR`) antes de disparar el POST a `/login`.

Todas las peticiones requieren un Bearer Token válido generado por Microsoft Entra ID en la cabecera Authorization.


1. Autoprovisión / Login
   Extrae los datos del usuario y sus roles (App Roles) desde el token de Azure, guardándolo en la base de datos si es su primer inicio de sesión.

```
POST /api/users/login
```
Respuesta Exitosa (200 OK):
```
{
"nombre": "Carlos Supervisor",
"email": "carlos@electrored.cl",
"rol": "SUPERVISOR",
"compania": "Empresa por Defecto",
"activo": true
}
```
2. Cambiar Estado del Usuario (Borrado Lógico)
   Habilita o deshabilita a un usuario en el sistema.

```
PUT /api/users/{azure_oid}/status
```

Body JSON
```
{
"active": false
}
```

3. Consultar datos usuario
```
http://localhost:8081/api/users/{azure_oid}
```