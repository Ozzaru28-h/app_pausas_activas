/** Mismo catálogo que ExerciseCatalog.kt en la app Android. */
export const EXERCISES = [
  { codigo: 'CUELLO_LATERAL', nombre: 'Movilidad de cuello', zona: 'Cabeza / cuello', icono: '🧘', color: '#7c5cff' },
  { codigo: 'ELEVACION_BRAZOS', nombre: 'Elevación de brazos y hombros', zona: 'Brazos / hombros', icono: '🙆', color: '#0ea5a4' },
  { codigo: 'FLEXION_LATERAL_TRONCO', nombre: 'Flexión lateral de tronco', zona: 'Tronco', icono: '🤸', color: '#f97316' }
];

export function exerciseInfo(codigo) {
  return EXERCISES.find((e) => e.codigo === codigo) ?? { icono: '💪', color: '#64748b', zona: 'General' };
}
