const mongoose = require('mongoose');

// Esquema para la colección "ejercicios"
const ejercicioSchema = new mongoose.Schema({
  codigo: { 
    type: String, 
    required: true, 
    unique: true 
  },
  nombre: { 
    type: String, 
    required: true 
  },
  descripcion: { 
    type: String 
  },
  angulo_optimo_min: { 
    type: Number, 
    required: true 
  },
  angulo_optimo_max: { 
    type: Number, 
    required: true 
  }
});

module.exports = mongoose.model('Ejercicio', ejercicioSchema);