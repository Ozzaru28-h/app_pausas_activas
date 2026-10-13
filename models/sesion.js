const mongoose = require('mongoose');

// Subdocumento para registrar cada error postural detectado
const metricaErrorSchema = new mongoose.Schema({
  articulacion_falla: { type: String, required: true },
  angulo_detectado: { type: Number, required: true },
  angulo_esperado: { type: Number, required: true }
}, { _id: false });

// Esquema principal para la colección "sesiones"
const sesionSchema = new mongoose.Schema({
  usuario_id: { 
    type: String, 
    required: [true, 'El ID del usuario es obligatorio'] 
  },
  fecha_registro: { 
    type: Date, 
    default: Date.now 
  },
  ejercicio: {
    codigo: { type: String, required: true },
    nombre: { type: String, required: true }
  },
  telemetria_ia: {
    postura_valida: { type: Boolean, required: true },
    precision_promedio_pct: { type: Number, required: true },
    metricas_error: [metricaErrorSchema] // Arreglo dinámico de objetos
  },
  notas_usuario: { 
    type: String, 
    default: '' 
  }
});

module.exports = mongoose.model('Sesion', sesionSchema);