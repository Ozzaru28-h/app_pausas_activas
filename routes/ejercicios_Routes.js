const express = require('express');
const router = express.Router();
const ejercicioController = require('../controllers/ejercicio_Controller');

router.post('/', ejercicioController.crearEjercicio);
router.get('/', ejercicioController.obtenerEjercicios);
router.get('/:id', ejercicioController.obtenerEjercicioPorId);

module.exports = router;