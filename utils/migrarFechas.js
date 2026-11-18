const Sesion = require('../models/sesion');

// Colombia no tiene horario de verano: siempre UTC-5.
const OFFSET_BOGOTA_HORAS = 5;

/**
 * Convierte un texto como "18/11/2026, 12:05:09 p. m." (hora de Bogotá, formato que
 * generaba toLocaleString('es-CO')) a un Date en UTC. También acepta fechas ISO.
 * Devuelve null si no se puede interpretar.
 */
function parsearFechaTexto(texto) {
  if (typeof texto !== 'string') return null;
  const limpio = texto.trim();

  const m = limpio.match(
    /^(\d{1,2})\/(\d{1,2})\/(\d{4}),?\s+(\d{1,2}):(\d{2})(?::(\d{2}))?\s*([ap])\.?\s*m\.?$/i
  );
  if (m) {
    const [, dia, mes, anio, hora, min, seg = '0', ampm] = m;
    let h = Number(hora) % 12;
    if (ampm.toLowerCase() === 'p') h += 12;
    return new Date(Date.UTC(
      Number(anio), Number(mes) - 1, Number(dia),
      h + OFFSET_BOGOTA_HORAS, Number(min), Number(seg)
    ));
  }

  // Fechas ISO guardadas como texto (p. ej. "2026-11-18T17:05:09.000Z")
  if (/^\d{4}-\d{2}-\d{2}T/.test(limpio)) {
    const d = new Date(limpio);
    return isNaN(d) ? null : d;
  }
  return null;
}

/**
 * Migración idempotente: busca sesiones con fecha_registro de tipo texto y la convierte
 * a Date. Se ejecuta al arrancar el servidor; si no hay nada que migrar no hace nada.
 */
async function migrarFechasTexto() {
  try {
    const coleccion = Sesion.collection;
    const pendientes = await coleccion
      .find({ fecha_registro: { $type: 'string' } }, { projection: { fecha_registro: 1 } })
      .toArray();
    if (pendientes.length === 0) return;

    const operaciones = [];
    const desdeId = [];
    for (const doc of pendientes) {
      let fecha = parsearFechaTexto(doc.fecha_registro);
      if (!fecha) {
        // Texto irreconocible: se usa el momento real de creación, guardado en el ObjectId.
        fecha = doc._id.getTimestamp();
        desdeId.push(`${doc._id} ("${doc.fecha_registro}")`);
      }
      operaciones.push({
        updateOne: { filter: { _id: doc._id }, update: { $set: { fecha_registro: fecha } } }
      });
    }

    await coleccion.bulkWrite(operaciones);
    console.log(`🛠️  Fechas migradas a Date: ${operaciones.length} sesión(es).`);
    if (desdeId.length) {
      console.warn(`⚠️  Fecha tomada del momento de creación del documento: ${desdeId.join(', ')}`);
    }
  } catch (error) {
    console.error('🔴 Error migrando fechas de sesiones:', error.message);
  }
}

module.exports = { migrarFechasTexto, parsearFechaTexto };
