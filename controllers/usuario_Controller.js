const Usuario = require('../models/usuarios');

// @desc    1. Registrar un nuevo usuario (HU-01)
// @route   POST /api/usuarios
exports.crearUsuario = async (req, res) => {
  try {
    const { correo, nombre, carrera } = req.body;

    // Validar que vengan los datos obligatorios
    if (!correo || !nombre) {
      return res.status(400).json({
        mensaje: 'Error de validación: El correo y el nombre son obligatorios.'
      });
    }

    // Regla de Negocio (HU-01): Validar dominio institucional (@uniautonoma.edu.co)
    const regexDominio = /^[a-zA-Z0-9._%+-]+@uniautonoma\.edu\.co$/;
    if (!regexDominio.test(correo)) {
      return res.status(400).json({
        mensaje: 'Error de validación: El correo debe pertenecer al dominio @uniautonoma.edu.co'
      });
    }

    // Verificar si el usuario ya existe
    const usuarioExiste = await Usuario.findOne({ correo });
    if (usuarioExiste) {
      return res.status(400).json({
        mensaje: 'El usuario con este correo ya se encuentra registrado.'
      });
    }

    // Crear el usuario en MongoDB
    const nuevoUsuario = await Usuario.create({
      correo,
      nombre,
      carrera
    });

    // Respuesta exitosa (Código HTTP 201)
    res.status(201).json({
      exito: true,
      mensaje: 'Usuario registrado exitosamente',
      datos: nuevoUsuario
    });

  } catch (error) {
    res.status(500).json({
      mensaje: 'Error en el servidor al crear usuario',
      error: error.message
    });
  }
};

// @desc    2. Obtener un usuario por su ID (HU-01)
// @route   GET /api/usuarios/:id
exports.obtenerUsuarioPorId = async (req, res) => {
  try {
    const usuario = await Usuario.findById(req.params.id);

    if (!usuario) {
      return res.status(404).json({
        mensaje: 'Usuario no encontrado'
      });
    }

    res.status(200).json({
      exito: true,
      datos: usuario
    });

  } catch (error) {
    res.status(500).json({
      mensaje: 'Error al consultar el usuario en la base de datos',
      error: error.message
    });
  }
};