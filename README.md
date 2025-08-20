# Gestión de Turnos

## Sobre el proyecto

Este proyecto surgió tras la finalización del curso de Programación III en la UTN Mar del Plata por un grupo de desarrolladores:
- Luca Mariño
- Rustam Sagaddinov  https://github.com/RustamLee
- Rebeca Camargo     https://github.com/rebekaa333

### 1.1 Propósito

El proyecto está pensado para que cualquier persona involucrada en el proyecto (equipo de desarrollo, docentes, testers) pueda entender qué hace el sistema, cómo funciona y qué se espera de él.

### 1.2 Ámbito del sistema

El sistema Gestión de Turnos es una aplicación web que permite gestionar reservas de salas dentro de una organización. Está pensado para que distintos tipos de usuarios —administradores, empleados y clientes— puedan interactuar con el sistema según sus permisos.

**Lo que el sistema hará:**
- Permitir a los usuarios registrarse e iniciar sesión.
- Visualizar salas disponibles y reservarlas según día, horario y capacidad.
- Los empleados podrán ver y modificar turnos.
- El administrador podrá gestionar (crear, modificar y eliminar) empleados.

**Lo que el sistema no hará:**
- No gestiona pagos en línea (aunque se registran métodos de pago como dato).
- No envía notificaciones automáticas por mail o SMS.
- No se diseñó para funcionar como app móvil nativa (solo web).

---

## Descripción general del sistema

### 2.1 Perspectiva del producto

El sistema **Gestión de Turnos** es una aplicación web que se desarrollará como un sistema independiente, aunque integrará una API externa: **Google Calendar**. Esta API permitirá sincronizar o registrar los turnos reservados en el calendario de Google, mejorando así la visibilidad y organización de los eventos.

La estructura está dividida en tres capas: backend, frontend y base de datos. Cada una tiene sus propias herramientas específicas para garantizar una arquitectura limpia y escalable.

### 2.2 Objetivos del sistema

**A grandes rasgos, el sistema permitirá:**
- **Usuarios:** registrarse, iniciar sesión, visualizar salas disponibles y reservarlas.
- **Empleados:** gestionar turnos, ver disponibilidad y modificar reservas.
- **Administradores:** gestionar empleados y tener control general sobre el sistema.

El sistema también mostrará calendarios, listados de turnos y permitirá aplicar filtros para facilitar la búsqueda.

### 2.3 Herramientas utilizadas

**Backend:**
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- JWT (manejo de autenticación)
- Maven
- Validación de formularios
- API externa: Google Calendar
- Lombok
- Control de versiones: Git + GitHub

> Repositorio: [https://github.com/RustamLee/GestionDeTurnos](https://github.com/RustamLee/GestionDeTurnos)  
> Testing: Postman  
> Documentación REST: Swagger  
> Acceso Swagger local: [http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)

**Base de Datos:**
- MySQL

**Frontend:**
- HTML
- CSS
- JavaScript
- Thymeleaf
- Toastr (notificaciones tipo toast)

**IDE:**
- IntelliJ IDEA CE

---

## Definición de requisitos del sistema

### 3.1 Requisitos funcionales

**Administrador:**
- ABM de empleados
- ABM de salas
- Visualizar todas las salas
- Visualizar todos los empleados

> ⚠️ El Administrador es un empleado técnico y no tiene acceso a operaciones comerciales como gestión de reservas.

**Empleado:**
- Iniciar sesión
- Visualizar todas las reservas activas
- Visualizar todas las salas
- Visualizar el calendario completo
- ABM de reservas
- Visualizar todos los clientes
- Visualizar su perfil

**Cliente:**
- Registrarse
- No repetir email existente
- Iniciar sesión
- Visualizar salas disponibles
- Visualizar reservas activas
- Visualizar y modificar su perfil
- ABM de sus reservas
- Visualizar calendario con reservas (solo horas y salas ocupadas de otros clientes sin datos personales)

### 3.2 Requisitos no funcionales

- Desarrollado bajo Spring Boot
- Base de datos MySQL
- Tiempo de respuesta < 2 segundos por operación
- Buenas prácticas (patrones, capas, control de versiones)
- Seguridad mediante JWT
- Documentación clara y accesible
- Ejecutable localmente sin dependencias externas complejas
- Compatible con navegadores modernos (Chrome, Firefox, Edge)  


### LOS DATOS DE PRUEBA

### EMPLEADOS (3)
1.
Nombre: Martín
Apellido: Pereyra
Teléfono: 2234567890
Email: martin@correo.com
DNI: 32145678

2.
Nombre: Carla
Apellido: Gutiérrez
Teléfono: 2231234567
Email: carla@correo.com
DNI: 29876543

3.
Nombre: Joaquín
Apellido: Roldán
Teléfono: 2236543210
Email: joaquin@correo.com
DNI: 33456789

### CLIENTES (3)
1.
Nombre: Florencia
Apellido: Suárez
Teléfono: 2237778899
Email: flor@correo.com
DNI: 27333444

2.
Nombre: Diego
Apellido: Molina
Teléfono: 2238889900
Email: diego@correo.com
DNI: 28555666

3.
Nombre: Camila
Apellido: Bustos
Teléfono: 2239990011
Email: camila@correo.com
DNI: 31222888

### SALAS (5)
1.
Número: 1
Nombre: Sala de piano

2.
Número: 2
Nombre: Sala de ensayo

3.
Número: 3
Nombre: Sala de guitarra

4.
Número: 4
Nombre: Sala de canto

5.
Número: 5
Nombre: Sala de rock
