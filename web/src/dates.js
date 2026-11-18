/**
 * Todas las fechas se interpretan y muestran en hora de Colombia (America/Bogota, UTC-5
 * sin horario de verano), sin importar la zona horaria del navegador.
 * En la base de datos fecha_registro se guarda como Date (UTC).
 */
export const TIME_ZONE = 'America/Bogota';
const OFFSET_BOGOTA_HORAS = 5;

/** "2026-11-10" (input type=date) → Date de las 00:00:00.000 de ese día en Bogotá. */
export function startOfDay(yyyyMmDd) {
  if (!yyyyMmDd) return undefined;
  const [y, m, d] = yyyyMmDd.split('-').map(Number);
  return new Date(Date.UTC(y, m - 1, d, OFFSET_BOGOTA_HORAS, 0, 0, 0));
}

/** "2026-11-10" → Date de las 23:59:59.999 de ese día en Bogotá. */
export function endOfDay(yyyyMmDd) {
  if (!yyyyMmDd) return undefined;
  const [y, m, d] = yyyyMmDd.split('-').map(Number);
  return new Date(Date.UTC(y, m - 1, d + 1, OFFSET_BOGOTA_HORAS, 0, 0, 0) - 1);
}

/** Fecha de hoy en Bogotá, en formato YYYY-MM-DD. */
export function todayInput() {
  // en-CA formatea como YYYY-MM-DD
  return new Intl.DateTimeFormat('en-CA', { timeZone: TIME_ZONE }).format(new Date());
}

const formatter = new Intl.DateTimeFormat('es-CO', {
  timeZone: TIME_ZONE,
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
  hour12: true
});

export function formatDate(iso) {
  if (!iso) return 'Fecha desconocida';
  const date = new Date(iso);
  return isNaN(date) ? iso : formatter.format(date);
}
