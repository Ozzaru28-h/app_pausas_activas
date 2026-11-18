# Respaldo de la base de datos `pausas_activas_db`

Exportado desde MongoDB Atlas el 2026-11-18, en formato Extended JSON (conserva los tipos `ObjectId` y `Date`).

| Archivo | Colección | Documentos |
|---|---|---|
| `usuarios.json` | usuarios | 3 |
| `sesions.json` | sesions | 4 |
| `ejercicios.json` | ejercicios | 4 |

> En `sesions.json` hay 2 sesiones con `fecha_registro` guardada como texto (formato es-CO).
> Son de cuando el modelo usaba `String`; la API las lee igual gracias a `utils/fechas.js`.

## Importar en otra base de datos

```bash
mongoimport --uri "<MONGO_URI>" --collection usuarios   --jsonArray --file database/usuarios.json
mongoimport --uri "<MONGO_URI>" --collection sesions    --jsonArray --file database/sesions.json
mongoimport --uri "<MONGO_URI>" --collection ejercicios --jsonArray --file database/ejercicios.json
```
