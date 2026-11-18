import { formatDate, formatRelative } from '../dates.js';
import { exerciseInfo } from '../exercises.js';

function PrecisionRing({ value, color }) {
  const pct = value != null ? Math.max(0, Math.min(100, Math.round(value))) : null;
  const r = 26;
  const c = 2 * Math.PI * r;
  return (
    <div className="ring" aria-label={pct != null ? `Precisión ${pct} %` : 'Precisión no disponible'}>
      <svg viewBox="0 0 64 64" width="64" height="64">
        <circle cx="32" cy="32" r={r} className="ring-track" />
        {pct != null && (
          <circle
            cx="32"
            cy="32"
            r={r}
            className="ring-value"
            stroke={color}
            strokeDasharray={`${(pct / 100) * c} ${c}`}
          />
        )}
      </svg>
      <span className="ring-label">{pct != null ? `${pct}%` : '—'}</span>
    </div>
  );
}

export default function SessionCard({ session, deleting, onDelete }) {
  const telemetry = session.telemetria_ia ?? {};
  const precision = telemetry.precision_promedio_pct;
  const valid = telemetry.postura_valida === true;
  const errors = telemetry.metricas_error ?? [];
  const info = exerciseInfo(session.ejercicio?.codigo);

  return (
    <li className="session" style={{ '--accent': info.color }}>
      <div className="session-icon" aria-hidden="true">
        {info.icono}
      </div>

      <div className="session-body">
        <div className="session-head">
          <div>
            <h3>{session.ejercicio?.nombre ?? 'Ejercicio sin nombre'}</h3>
            <p className="muted small">
              <span className="zone">{info.zona}</span>
              <span className="dot">•</span>
              <span title={formatDate(session.fecha_registro)}>{formatRelative(session.fecha_registro)}</span>
              <span className="dot">•</span>
              {formatDate(session.fecha_registro)}
            </p>
          </div>
          <span className={`badge ${valid ? 'ok' : 'warn'}`}>
            {valid ? '✓ Postura válida' : '⚠ Por mejorar'}
          </span>
        </div>

        {errors.length > 0 && (
          <ul className="chips">
            {errors.map((err, i) => (
              <li key={i} className="chip">
                <strong>{err.articulacion_falla ?? 'Articulación'}</strong>
                <span>
                  {Math.round(err.angulo_detectado ?? 0)}° / {Math.round(err.angulo_esperado ?? 0)}° esperado
                </span>
              </li>
            ))}
          </ul>
        )}

        {session.notas_usuario && <p className="note">“{session.notas_usuario}”</p>}
      </div>

      <div className="session-side">
        <PrecisionRing value={precision} color={info.color} />
        <button
          type="button"
          className="icon-btn danger"
          onClick={onDelete}
          disabled={deleting}
          title="Eliminar pausa"
          aria-label="Eliminar pausa"
        >
          {deleting ? '…' : '🗑'}
        </button>
      </div>
    </li>
  );
}
