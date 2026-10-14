const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middlewares
app.use(cors());
app.use(express.json());


// Conexión a MongoDB Atlas
mongoose.connect(process.env.MONGO_URI)
  .then(() => console.log('🟢 ¡Conexión exitosa a MongoDB Atlas!'))
  .catch((error) => console.log('🔴 Error conectando a MongoDB:', error.message));

// RUTAS DE LA API
app.use('/api/usuarios', require('./routes/usuario_Routes'));
app.use('/api/sesiones', require('./routes/sesion_Routes'));
app.use('/api/ejercicios', require('./routes/ejercicios_Routes'));

// Ruta de prueba inicial
app.get('/', (req, res) => {
  res.send('🚀 ¡La API de Pausas Activas está funcionando al 100%!');
});

// Encender servidor
app.listen(PORT, () => {
  console.log(`🔥 Servidor corriendo en http://localhost:${PORT}`);
});