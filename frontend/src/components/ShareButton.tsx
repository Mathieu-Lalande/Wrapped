import type { ExportStatus } from '../hooks/useExportPng';
import styles from './ShareButton.module.css';

interface ShareButtonProps {
  readonly status: ExportStatus;
  readonly onShare: () => void;
}

const LABELS: Record<ExportStatus, string> = {
  idle: 'Partager',
  exporting: 'Export…',
  error: 'Réessayer',
};

export function ShareButton({ status, onShare }: ShareButtonProps) {
  return (
    <button
      type="button"
      className={styles.button}
      onClick={(event) => {
        event.stopPropagation();
        onShare();
      }}
      onPointerDown={(event) => event.stopPropagation()}
      onPointerUp={(event) => event.stopPropagation()}
      disabled={status === 'exporting'}
      aria-label={status === 'error' ? "Échec de l'export, réessayer" : 'Partager cette carte en image PNG'}
      data-export-ignore=""
    >
      <svg className={styles.icon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" aria-hidden="true">
        <path d="M12 3v12M7 8l5-5 5 5M5 14v5a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-5" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
      {LABELS[status]}
    </button>
  );
}
