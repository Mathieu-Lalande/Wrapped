import { motion } from 'framer-motion';

import type { ApiError } from '../api/wrappedApi';
import styles from './StatusScreen.module.css';

export function LoadingScreen({ login }: { readonly login: string }) {
  return (
    <main className={styles.screen} aria-busy="true">
      <div className={styles.box} role="status">
        <div className={styles.bars} aria-hidden="true">
          {[0, 1, 2, 3, 4].map((i) => (
            <motion.span
              key={i}
              className={styles.bar}
              animate={{ height: ['20%', '100%', '35%', '80%', '20%'] }}
              transition={{ duration: 1.4, repeat: Infinity, delay: i * 0.12, ease: 'easeInOut' }}
            />
          ))}
        </div>
        <p className={styles.title}>On compte tes commits…</p>
        <p className={styles.text}>Analyse de l’année de @{login}.</p>
      </div>
    </main>
  );
}

interface ErrorScreenProps {
  readonly error: ApiError;
  readonly onRetry: () => void;
  readonly onBack: () => void;
}

export function ErrorScreen({ error, onRetry, onBack }: ErrorScreenProps) {
  const notFound = error.status === 404;
  const retryable = error.status !== 400 && !notFound;

  return (
    <main className={styles.screen}>
      <div className={styles.box} role="alert">
        <p className={styles.title}>{notFound ? 'Introuvable' : 'Oups.'}</p>
        <p className={styles.text}>
          {error.message}
          {error.retryAfterSeconds !== null && ` (réessaie dans ${error.retryAfterSeconds} s)`}
        </p>
        <div className={styles.actions}>
          {retryable && (
            <button type="button" className={styles.primary} onClick={onRetry}>
              Réessayer
            </button>
          )}
          <button type="button" className={retryable ? styles.secondary : styles.primary} onClick={onBack}>
            Changer de login
          </button>
        </div>
      </div>
    </main>
  );
}
