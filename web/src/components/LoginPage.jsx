import { useState } from 'react';
import { login, register, API_URL } from '../api.js';

const EMAIL_REGEX = /^[A-Za-z0-9._%+-]+@uniautonoma\.edu\.co$/;

export default function LoginPage({ onLogin }) {
  const [registerMode, setRegisterMode] = useState(false);
  const [correo, setCorreo] = useState('');
  const [nombre, setNombre] = useState('');
  const [carrera, setCarrera] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    const email = correo.trim().toLowerCase();
    if (!EMAIL_REGEX.test(email)) {
      setError('Usa tu correo institucional (termina en @uniautonoma.edu.co).');
      return;
    }
    if (registerMode && !nombre.trim()) {
      setError('El nombre es obligatorio.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const user = registerMode
        ? await register({ correo: email, nombre: nombre.trim(), carrera: carrera.trim() })
        : await login(email);
      onLogin(user);
    } catch (e) {
      setError(e.message);
      setLoading(false);
    }
  };

  const toggleMode = () => {
    setRegisterMode(!registerMode);
    setError(null);
  };

  return (
    <main className="login">
      <section className="login-art" aria-hidden="true">
        <div className="brand light">
          <span className="logo">🌿</span>
          <span>Pausas Activas</span>
        </div>
        <div>
          <h2>Muévete, respira y vuelve con energía.</h2>
          <p>Revisa tus pausas activas, tu precisión de postura y los puntos a mejorar, todo sincronizado con la app móvil.</p>
        </div>
        <div className="floating">
          <span>🧘</span>
          <span>🙆</span>
          <span>🤸</span>
        </div>
      </section>

      <section className="login-side">
        <form className="login-card" onSubmit={handleSubmit}>
          <div className="tabs" role="tablist">
            <button type="button" role="tab" aria-selected={!registerMode} className={!registerMode ? 'active' : ''} onClick={() => registerMode && toggleMode()}>
              Ingresar
            </button>
            <button type="button" role="tab" aria-selected={registerMode} className={registerMode ? 'active' : ''} onClick={() => !registerMode && toggleMode()}>
              Registrarme
            </button>
          </div>

          <div>
            <h1>{registerMode ? 'Crea tu cuenta' : 'Bienvenido de nuevo'}</h1>
            <p className="muted">
              {registerMode ? 'Regístrate con tu correo institucional' : 'Ingresa con tu correo institucional'}
            </p>
          </div>

          <label>
            <span>Correo institucional</span>
            <input
              type="email"
              value={correo}
              onChange={(e) => {
                setCorreo(e.target.value);
                setError(null);
              }}
              placeholder="nombre.apellido@uniautonoma.edu.co"
              autoFocus
            />
          </label>

          {registerMode && (
            <>
              <label>
                <span>Nombre</span>
                <input
                  value={nombre}
                  onChange={(e) => {
                    setNombre(e.target.value);
                    setError(null);
                  }}
                  placeholder="Tu nombre completo"
                />
              </label>
              <label>
                <span>Carrera (opcional)</span>
                <input value={carrera} onChange={(e) => setCarrera(e.target.value)} placeholder="Ej. Ingeniería de Software" />
              </label>
            </>
          )}

          {error && (
            <p className="alert error" role="alert">
              {error}
            </p>
          )}

          <button type="submit" className="primary big" disabled={loading}>
            {loading ? 'Cargando…' : registerMode ? 'Crear cuenta' : 'Ingresar'}
          </button>

          <p className="small muted center-text">
            Acceso de demostración: solo se comprueba que el correo esté registrado.
            <br />
            Servidor: <code>{API_URL}</code>
          </p>
        </form>
      </section>
    </main>
  );
}
