const Sesion = require('../models/sesion');

// @desc    1. Registrar una nueva sesión con telemetría de IA (HU-07)
// @route   POST /api/sesiones
exports.crearSesion = async (req, res) => {
  try {
    const { usuario_id, ejercicio, telemetria_ia, notas_usuario } = req.body;

    if (!usuario_id || !ejercicio || !telemetria_ia) {
      return res.status(400).json({
        mensaje: 'Error: usuario_id, ejercicio y telemetria_ia son obligatorios.'
      });
    }

    const nuevaSesion = await Sesion.create({
      usuario_id,
      ejercicio,
      telemetria_ia,
      notas_usuario
    });

    res.status(201).json({
      exito: true,
      mensaje: 'Sesión guardada exitosamente en MongoDB',
      datos: nuevaSesion
    });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al registrar la sesión', error: error.message });
  }
};

// @desc    2. Listar historial de sesiones de un usuario (HU-08/HU-09)
// @route   GET /api/sesiones/usuario/:usuario_id
exports.obtenerSesionesPorUsuario = async (req, res) => {
  try {
    const sesiones = await Sesion.find({ usuario_id: req.params.usuario_id }).sort({ fecha_registro: -1 });
    res.status(200).json({ exito: true, total: sesiones.length, datos: sesiones });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al consultar sesiones del usuario', error: error.message });
  }
};

// @desc    3. Obtener el detalle de una sesión por ID (HU-09)
// @route   GET /api/sesiones/:id
exports.obtenerSesionPorId = async (req, res) => {
  try {
    const sesion = await Sesion.findById(req.params.id);
    if (!sesion) {
      return res.status(404).json({ mensaje: 'Sesión no encontrada' });
    }
    res.status(200).json({ exito: true, datos: sesion });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al consultar la sesión', error: error.message });
  }
};

// @desc    4. Actualizar notas de una sesión
// @route   PUT /api/sesiones/:id
exports.actualizarNotasSesion = async (req, res) => {
  try {
    const { notas_usuario } = req.body;
    const sesionActualizada = await Sesion.findByIdAndUpdate(
      req.params.id,
      { notas_usuario },
      { new: true }
    );
    if (!sesionActualizada) {
      return res.status(404).json({ mensaje: 'Sesión no encontrada' });
    }
    res.status(200).json({ exito: true, mensaje: 'Notas actualizadas correctamente', datos: sesionActualizada });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al actualizar notas de la sesión', error: error.message });
  }
};

// @desc    5. Eliminar una sesión
// @route   DELETE /api/sesiones/:id
exports.eliminarSesion = async (req, res) => {
  try {
    const sesionEliminada = await Sesion.findByIdAndDelete(req.params.id);
    if (!sesionEliminada) {
      return res.status(404).json({ mensaje: 'Sesión no encontrada' });
    }
    res.status(200).json({ exito: true, mensaje: 'Sesión eliminada de MongoDB' });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al eliminar la sesión', error: error.message });
  }
};

// @desc    6. Analítica Avanzada: Top 3 fallas posturales (Aggregation Pipeline - HU-10)
// @route   GET /api/sesiones/analitica/errores
exports.obtenerAnaliticaErrores = async (req, res) => {
  try {
    const reporte = await Sesion.aggregate([
      { $unwind: "$telemetria_ia.metricas_error" },
      {
        $group: {
          _id: "$telemetria_ia.metricas_error.articulacion_falla",
          total_incidencias: { $sum: 1 },
          promedio_angulo_detectado: { $avg: "$telemetria_ia.metricas_error.angulo_detectado" }
        }
      },
      { $sort: { total_incidencias: -1 } },
      { $limit: 3 }
    ]);

    res.status(200).json({
      exito: true,
      mensaje: 'Top 3 de fallas posturales más comunes',
      datos: reporte
    });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al generar la analítica', error: error.message });
  }
};