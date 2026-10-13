const Sesion = require('../models/sesion');

// 1. Guardar una sesión finalizada con telemetría de IA (HU-07)
exports.crearSesion = async (req, res) => {
  try {
    const { usuario_id, ejercicio, telemetria_ia, notas_usuario } = req.body;

    if (!usuario_id || !ejercicio || !telemetria_ia) {
      return res.status(400).json({
        mensaje: 'Error de validación: usuario_id, ejercicio y telemetria_ia son obligatorios.'
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
      mensaje: 'Sesión de pausa activa registrada exitosamente en MongoDB',
      datos: nuevaSesion
    });
  } catch (error) {
    res.status(500).json({
      mensaje: 'Error al registrar la sesión',
      error: error.message
    });
  }
};

// 2. Obtener todas las sesiones de un usuario (HU-08 / HU-09)
exports.obtenerSesionesPorUsuario = async (req, res) => {
  try {
    const sesiones = await Sesion.find({ usuario_id: req.params.usuario_id }).sort({ fecha_registro: -1 });
    res.status(200).json({ exito: true, total: sesiones.length, datos: sesiones });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al consultar sesiones', error: error.message });
  }
};

// 3. Obtener el detalle de una sesión específica (HU-09)
exports.obtenerSesionPorId = async (req, res) => {
  try {
    const sesion = await Sesion.findById(req.params.id);
    if (!sesion) return res.status(404).json({ mensaje: 'Sesión no encontrada' });
    res.status(200).json({ exito: true, datos: sesion });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al consultar la sesión', error: error.message });
  }
};

// 4. Actualizar notas de la sesión
exports.actualizarNotasSesion = async (req, res) => {
  try {
    const sesionActualizada = await Sesion.findByIdAndUpdate(
      req.params.id,
      { notas_usuario: req.body.notas_usuario },
      { new: true }
    );
    if (!sesionActualizada) return res.status(404).json({ mensaje: 'Sesión no encontrada' });
    res.status(200).json({ exito: true, mensaje: 'Notas actualizadas', datos: sesionActualizada });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al actualizar notas', error: error.message });
  }
};

// 5. Eliminar una sesión
exports.eliminarSesion = async (req, res) => {
  try {
    const sesionEliminada = await Sesion.findByIdAndDelete(req.params.id);
    if (!sesionEliminada) return res.status(404).json({ mensaje: 'Sesión no encontrada' });
    res.status(200).json({ exito: true, mensaje: 'Sesión eliminada de MongoDB' });
  } catch (error) {
    res.status(500).json({ mensaje: 'Error al eliminar sesión', error: error.message });
  }
};

// 6. Analítica Avanzada: Top 3 de fallas más comunes (HU-10 / Aggregation Pipeline)
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