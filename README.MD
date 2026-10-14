# API REST - Sistema de Pausas Activas Inteligentes

API REST desarrollada en Node.js, Express y MongoDB Atlas para gestionar la telemetría, usuarios y analítica de pausas activas evaluadas con Inteligencia Artificial.

## Tecnologías Utilizadas
* **Entorno:** Node.js
* **Framework:** Express.js[cite: 8]
* **Base de Datos:** MongoDB Atlas (Mongoose ODM)[cite: 8]
* **Validación de Entorno:** Dotenv (Manejo seguro de credenciales)[cite: 8, 11]

## Tabla de Historias de Usuario y Funcionalidades MVP

| N.º | Historia de Usuario | Funcionalidad | Endpoint y Método HTTP | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| 1 | Como usuario, quiero registrar mi perfil con correo institucional, para asociar mis métricas. | Crear usuario (Validación Regex) | `POST /api/usuarios` | 201 Created / 400 Bad Request[cite: 8, 11] |
| 2 | Como usuario, quiero consultar mi información registrada, para verificar mi perfil. | Obtener usuario por ID | `GET /api/usuarios/:id` | 200 OK / 404 Not Found[cite: 8] |
| 3 | Como administrador, quiero registrar ejercicios con sus límites angulares, para la IA. | Crear ejercicio | `POST /api/ejercicios` | 201 Created[cite: 8, 11] |
| 4 | Como usuario, quiero ver los ejercicios disponibles, para elegir mi rutina. | Listar ejercicios | `GET /api/ejercicios` | 200 OK[cite: 8] |
| 5 | Como sistema, quiero obtener la configuración de un ejercicio, para calibrar ángulos. | Obtener ejercicio por ID | `GET /api/ejercicios/:id` | 200 OK / 404 Not Found[cite: 8] |
| 6 | Como sistema, quiero guardar la telemetría de la IA y fallas articulares, para evaluar. | Registrar sesión de pausa | `POST /api/sesiones` | 201 Created[cite: 8, 11] |
| 7 | Como usuario, quiero ver mi historial de pausas activas, para monitorear mi avance. | Listar sesiones por usuario | `GET /api/sesiones/usuario/:usuario_id` | 200 OK[cite: 8] |
| 8 | Como usuario, quiero ver el detalle técnico de una sesión, para analizar mi postura. | Obtener detalle de sesión | `GET /api/sesiones/:id` | 200 OK / 404 Not Found[cite: 8] |
| 9 | Como usuario, quiero agregar observaciones a mi sesión, para llevar mi diario de salud. | Actualizar notas de sesión | `PUT /api/sesiones/:id` | 200 OK[cite: 8, 11] |
| 10 | Como supervisor, quiero consultar el Top 3 de fallas comunes, para ajustar campañas. | Analítica de fallas (Aggregation) | `GET /api/sesiones/analitica/errores` | 200 OK[cite: 4, 8] |
| 11 | Como usuario, quiero eliminar una sesión descartada, para no alterar mis promedios. | Eliminar sesión | `DELETE /api/sesiones/:id` | 200 OK[cite: 8, 11] |

## Instrucciones de Ejecución
1. Clonar el repositorio.
2. Ejecutar `npm install`.
3. Configurar el archivo `.env` basándose en `.env.example`.
4. Iniciar el servidor con `node index.js`.