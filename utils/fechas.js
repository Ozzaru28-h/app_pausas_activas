/**
 * Utilidades para fecha_registro.
 *
 * Durante un tiempo fecha_registro se guardó como TEXTO en formato es-CO
 * (p. ej. "18/11/2026, 12:26:12 p. m.", hora de Bogotá). Al compararlo con un
 * Date en un filtro $gte/$lte, MongoDB compara cadenas y el filtro "desde"
 * nunca coincidía. Ahora se guarda como Date, y estas funciones permiten
 * seguir leyendo los registros antiguos.
 */

// "dd/mm/yyyy, h:mm[:ss] a. m./p. m." (con o sin puntos/espacios raros)
const ES_CO_REGEX =
  /^(\d{1,2})\/(\d{1,2})\/(\d{4}),?\s+(\d{1,2}):(\d{2})(?::(\d{2}))?\s*([ap])\.?\s*m\.?$/i;

const BOGOTA_OFFSET_HOURS = 5; // America/Bogota = UTC-5 (sin horario de verano)

/** Convierte cualquier formato conocido de fecha_registro a Date (o null). */
function parseFechaRegistro(valor) {
  if (valor == null || valor === '') return null;
  if (valor instanceof Date) return isNaN(valor) ? null : valor;

  const texto = String(valor).replace(/ | /g, ' ').trim();
  const m = texto.match(ES_CO_REGEX);
  if (m) {
    const [, d, mes, y, h, min, s = '0', ampm] = m;
    let hora = Number(h) % 12;
    if (ampm.toLowerCase() === 'p') hora += 12;
    return new Date(Date.UTC(Number(y), Number(mes) - 1, Number(d), hora + BOGOTA_OFFSET_HOURS, Number(min), Number(s)));
  }

  const iso = new Date(texto);
  return isNaN(iso) ? null : iso;
}

/** Devuelve una copia de la sesión (objeto plano) con fecha_registro en ISO. */
function normalizarSesion(sesion) {
  const fecha = parseFechaRegistro(sesion.fecha_registro);
  return { ...sesion, fecha_registro: fecha ? fecha.toISOString() : sesion.fecha_registro };
}

module.exports = { parseFechaRegistro, normalizarSesion };
