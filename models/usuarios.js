const mongoose = require('mongoose');

// Esquema para la colección "usuarios"
const usuarioSchema = new mongoose.Schema({
  correo: { 
    type: String, 
    required: [true, 'El correo institucional es obligatorio'], 
    unique: true,
    trim: true
  },
  nombre: { 
    type: String, 
    required: [true, 'El nombre es obligatorio'] 
  },
  carrera: { 
    type: String, 
    default: 'Ingeniería de Sistemas' 
  },
  fecha_registro: { 
    type: Date, 
    default: Date.now 
  }
});

module.exports = mongoose.model('Usuario', usuarioSchema);