const express = require('express');
const router = express.Router();
const sesionController = require('../controllers/sesion_Controller');

router.post('/', sesionController.crearSesion);
router.get('/usuario/:usuario_id', sesionController.obtenerSesionesPorUsuario);
router.get('/analitica/errores', sesionController.obtenerAnaliticaErrores);
router.get('/:id', sesionController.obtenerSesionPorId);
router.put('/:id', sesionController.actualizarNotasSesion);
router.delete('/:id', sesionController.eliminarSesion);

module.exports = router;