const express = require('express');
const router = express.Router();
const usuarioController = require('../controllers/usuario_Controller');

// Definición de Endpoints para /api/usuarios
router.post('/', usuarioController.crearUsuario);
router.get('/:id', usuarioController.obtenerUsuarioPorId);

module.exports = router;