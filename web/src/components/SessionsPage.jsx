import { useCallback, useEffect, useRef, useState } from 'react';
import { deleteSession, getSessions } from '../api.js';
import { daysAgoInput, endOfDay, startOfDay, todayInput } from '../dates.js';
import { EXERCISES } from '../exercises.js';
import SessionCard from './SessionCard.jsx';

/** Cada cuánto se vuelve a consultar la API para ver las sesiones nuevas hechas desde el móvil. */
const AUTO_REFRESH_MS = 20000;

export default function SessionsPage({ user, onLogout }) {
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const [ejercicio, setEjercicio] = useState('');

  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [deletingId, setDeletingId] = useState(null);
  const [notice, setNotice] = useState(null);

  // Evita que una respuesta vieja pise a una más reciente si los filtros cambian rápido.
  const requestIdRef = useRef(0);

  const rangeError = desde && hasta && desde > hasta
    ? 'La fecha "desde" no puede ser posterior a la fecha "hasta".'
    : null;

  const load = useCallback(
    async ({ silent = false } = {}) => {
      if (rangeError) return;
      const requestId = ++requestIdRef.current;
      if (!silent) setLoading(true);
      try {
        const data = await getSessions(user.id, {
          desde: startOfDay(desde),
          hasta: endOfDay(hasta),
          ejercicio: ejercicio || undefined
        });
        if (requestId === requestIdRef.current) {
          setSessions(data);
          setError(null);
        }
      } catch (e) {
        if (requestId === requestIdRef.current) setError(e.message);
      } finally {
        if (requestId === requestIdRef.current) setLoading(false);
      }
    },
    [user.id, desde, hasta, ejercicio, rangeError]
  );

  useEffect(() => {
    load();
  }, [load]);

  // Sincronización con el móvil: refresco periódico y al volver a la pestaña.
  useEffect(() => {
    const interval = setInterval(() => load({ silent: true }), AUTO_REFRESH_MS);
    const onFocus = () => load({ silent: true });
    window.addEventListener('focus', onFocus);
    return () => {
      clearInterval(interval);
      window.removeEventListener('focus', onFocus);
    };
  }, [load]);

  const handleDelete = async (session) => {
    const ok = window.confirm(
      `¿Eliminar la pausa "${session.ejercicio?.nombre ?? 'sin nombre'}"? ` +
        'También desaparecerá de la app móvil. Esta acción no se puede deshacer.'
    );
    if (!ok) return;

    setDeletingId(session._id);
    try {
      await deleteSession(session._id);
      setSessions((prev) => prev.filter((s) => s._id !== session._id));
      setNotice('Pausa eliminada.');
      setTimeout(() => setNotice(null), 3000);
    } catch (e) {
      setError(e.message);
    } finally {
      setDeletingId(null);
    }
  };

  const setRange = (days) => {
    setDesde(days === 0 ? todayInput() : daysAgoInput(days));
    setHasta(todayInput());
  };

  const clearFilters = () => {
    setDesde('');
    setHasta('');
    setEjercicio('');
  };

  const total = sessions.length;
  const validas = sessions.filter((s) => s.telemetria_ia?.postura_valida).length;
  const precisionPromedio = total
    ? Math.round(
        sessions.reduce((acc, s) => acc + (s.telemetria_ia?.precision_promedio_pct ?? 0), 0) / total
      )
    : null;
  const hasFilters = Boolean(desde || hasta || ejercicio);
  const initials = (user.nombre || user.correo || '?')
    .split(/[s.@]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join('');
  const activeRange =
    hasta === todayInput() ? [0, 7, 30].find((d) => desde === (d === 0 ? todayInput() : daysAgoInput(d))) : undefined;

  return (
    <div className="app">
      <header className="hero">
        <div className="hero-inner">
          <div className="brand">
            <span className="logo" aria-hidden="true">🌿</span>
            <span>Pausas Activas</span>
          </div>
          <div className="user">
            <div className="user-text">
              <strong>{user.nombre || 'Usuario'}</strong>
              <span>{user.correo}</span>
            </div>
            <span className="avatar" aria-hidden="true">{initials}</span>
            <button type="button" className="ghost-light" onClick={onLogout}>
              Salir
            </button>
          </div>
        </div>
        <div className="hero-inner hero-title">
          <h1>¡Hola{user.nombre ? `, ${user.nombre.split(' ')[0]}` : ''}! 👋</h1>
          <p>Este es el historial de tus pausas activas, sincronizado con la app móvil.</p>
        </div>
      </header>

      <main className="page">
        <section className="stats">
          <div className="stat">
            <span className="stat-icon" aria-hidden="true">📋</span>
            <div>
              <span className="stat-value">{total}</span>
              <span className="stat-label">{hasFilters ? 'sesiones encontradas' : 'sesiones en total'}</span>
            </div>
          </div>
          <div className="stat">
            <span className="stat-icon ok" aria-hidden="true">✅</span>
            <div>
              <span className="stat-value">{validas}</span>
              <span className="stat-label">con postura válida</span>
            </div>
          </div>
          <div className="stat">
            <span className="stat-icon accent" aria-hidden="true">🎯</span>
            <div>
              <span className="stat-value">{precisionPromedio}%</span>
              <span className="stat-label">precisión promedio</span>
              <span className="bar"><span style={{ width: `${precisionPromedio}%` }} /></span>
            </div>
          </div>
        </section>

        <section className="panel filters" aria-label="Filtros de búsqueda">
          <div className="filters-row">
            <div className="quick">
              {[
                [0, 'Hoy'],
                [7, '7 días'],
                [30, '30 días']
              ].map(([days, label]) => (
                <button
                  key={days}
                  type="button"
                  className={`pill ${activeRange === days ? 'active' : ''}`}
                  onClick={() => setRange(days)}
                >
                  {label}
                </button>
              ))}
            </div>
            <div className="dates">
              <label>
                <span>Desde</span>
                <input type="date" value={desde} max={hasta || undefined} onChange={(e) => setDesde(e.target.value)} />
              </label>
              <span className="arrow" aria-hidden="true">→</span>
              <label>
                <span>Hasta</span>
                <input type="date" value={hasta} min={desde || undefined} onChange={(e) => setHasta(e.target.value)} />
              </label>
            </div>
          </div>

          <div className="filters-row">
            <div className="exercise-chips" role="radiogroup" aria-label="Ejercicio">
              <button
                type="button"
                role="radio"
                aria-checked={!ejercicio}
                className={`pill ${!ejercicio ? 'active' : ''}`}
                onClick={() => setEjercicio('')}
              >
                Todos
              </button>
              {EXERCISES.map((ex) => (
                <button
                  key={ex.codigo}
                  type="button"
                  role="radio"
                  aria-checked={ejercicio === ex.codigo}
                  className={`pill ${ejercicio === ex.codigo ? 'active' : ''}`}
                  style={{ '--accent': ex.color }}
                  onClick={() => setEjercicio(ex.codigo)}
                  title={ex.zona}
                >
                  <span aria-hidden="true">{ex.icono}</span> {ex.nombre}
                </button>
              ))}
            </div>
            <div className="filter-actions">
              <button type="button" className="ghost" onClick={clearFilters} disabled={!hasFilters}>
                Limpiar
              </button>
              <button type="button" className="primary" onClick={() => load()} disabled={loading}>
                {loading ? 'Cargando…' : '↻ Actualizar'}
              </button>
            </div>
          </div>
        </section>

        {rangeError && <p className="alert error">{rangeError}</p>}
        {error && (
          <p className="alert error" role="alert">
            {error}
          </p>
        )}
        {notice && <p className="toast">{notice}</p>}

        {loading ? (
          <ul className="session-list">
            {[0, 1, 2].map((i) => (
              <li key={i} className="session skeleton" />
            ))}
          </ul>
        ) : sessions.length === 0 ? (
          <div className="empty">
            <span aria-hidden="true">{hasFilters ? '🔍' : '🧘'}</span>
            <h3>{hasFilters ? 'Sin resultados' : 'Aún no hay pausas'}</h3>
            <p className="muted">
              {hasFilters
                ? 'No hay sesiones que coincidan con la búsqueda. Prueba con otro rango de fechas.'
                : 'Completa un ejercicio en la app móvil para verlo aquí.'}
            </p>
            {hasFilters && (
              <button type="button" className="ghost" onClick={clearFilters}>
                Quitar filtros
              </button>
            )}
          </div>
        ) : (
          <ul className="session-list">
            {sessions.map((s) => (
              <SessionCard
                key={s._id}
                session={s}
                deleting={deletingId === s._id}
                onDelete={() => handleDelete(s)}
              />
            ))}
          </ul>
        )}
      </main>
    </div>
  );
}
