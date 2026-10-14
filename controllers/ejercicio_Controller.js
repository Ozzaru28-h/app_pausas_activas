const Ejercicio = require('../models/ejercicio');

// @desc    1. Crear un nuevo ejercicio/rutina
// @route   POST /api/ejercicios
exports.crearEjercicio = async (req, res) => {
  try {
    const { codigo, nombre, descripcion, angulo_optimo_min, angulo_optimo_max } = req.body;

    if (!codigo || !nombre || angulo_optimo_min === undefined || angulo_optimo_max === undefined) {
      return res.status(400).json({
        mensaje: 'Error: codigo, nombre, angulo_optimo_min y angulo_optimo_max son obligatorios.'
      });
    }

    const ejercicioExiste = await Ejercicio.findOne({ codigo });
    if (ejercicioExiste) {
      return res.status(400).json({
        mensaje: 'Ya existe un ejercicio con este código.'
      });
    }

    const nuevoEjercicio = await Ejercicio.create({
      codigo,
      nombre,
      descripcion,
      angulo_optimo_min,
      angulo_optimo_max
    });

    res.status(201).json({
      exito: true,
      mensaje: 'Ejercicio creado exitosamente',
      datos: nuevoEjercicio
    });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al crear ejercicio', error: error.message });
  }
};

// @desc    2. Listar todos los ejercicios disponibles (HU-03)
// @route   GET /api/ejercicios
exports.obtenerEjercicios = async (req, res) => {
  try {
    const ejercicios = await Ejercicio.find();
    res.status(200).json({
      exito: true,
      total: ejercicios.length,
      datos: ejercicios
    });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al obtener ejercicios', error: error.message });
  }
};

// @desc    3. Obtener un ejercicio por su ID (HU-03)
// @route   GET /api/ejercicios/:id
exports.obtenerEjercicioPorId = async (req, res) => {
  try {
    const ejercicio = await Ejercicio.findById(req.params.id);
    if (!ejercicio) {
      return res.status(404).json({ mensaje: 'Ejercicio no encontrado' });
    }
    res.status(200).json({ exito: true, datos: ejercicio });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al obtener el ejercicio', error: error.message });
  }
};