import { useId, useState, type FormEvent } from 'react';
import { motion } from 'framer-motion';

import { availableYears, isValidLogin } from '../lib/validation';
import styles from './HomePage.module.css';

interface HomePageProps {
  readonly onSubmit: (login: string, year: number) => void;
  readonly initialLogin?: string;
}

const LOGIN_ERROR = 'Login invalide : 1 à 39 caractères, lettres, chiffres ou tirets.';

export function HomePage({ onSubmit, initialLogin = '' }: HomePageProps) {
  const years = availableYears();
  const [login, setLogin] = useState(initialLogin);
  const [year, setYear] = useState<number>(years[0] ?? new Date().getUTCFullYear());
  const [error, setError] = useState<string | null>(null);
  const loginId = useId();
  const yearId = useId();
  const errorId = useId();

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const normalized = login.trim().replace(/^@/, '');
    if (!isValidLogin(normalized)) {
      setError(LOGIN_ERROR);
      return;
    }
    setError(null);
    onSubmit(normalized, year);
  };

  return (
    <main className={styles.page}>
      <motion.div
        className={styles.inner}
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
      >
        <header>
          <p className={styles.kicker}>GitHub Wrapped</p>
          <h1 className={styles.title}>
            Ton année
            <em>en commits.</em>
          </h1>
          <p className={styles.subtitle}>
            Contributions, séries, repos, langages : ton année GitHub racontée en cartes à partager.
          </p>
        </header>

        <form className={styles.form} onSubmit={handleSubmit} noValidate>
          <div className={styles.field}>
            <label className={styles.label} htmlFor={loginId}>
              Login GitHub
            </label>
            <div className={styles.inputWrap} data-invalid={error !== null}>
              <span className={styles.at} aria-hidden="true">
                @
              </span>
              <input
                id={loginId}
                className={styles.input}
                value={login}
                onChange={(event) => {
                  setLogin(event.target.value);
                  if (error !== null) {
                    setError(null);
                  }
                }}
                placeholder="octocat"
                autoComplete="off"
                autoCapitalize="none"
                autoCorrect="off"
                spellCheck={false}
                maxLength={40}
                aria-invalid={error !== null}
                aria-describedby={error !== null ? errorId : undefined}
                required
              />
            </div>
            {error !== null && (
              <p id={errorId} className={styles.error} role="alert">
                {error}
              </p>
            )}
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label className={styles.label} htmlFor={yearId}>
                Année
              </label>
              <select
                id={yearId}
                className={styles.select}
                value={year}
                onChange={(event) => setYear(Number(event.target.value))}
              >
                {years.map((y) => (
                  <option key={y} value={y}>
                    {y}
                  </option>
                ))}
              </select>
            </div>
            <button type="submit" className={styles.submit}>
              Lancer mon Wrapped →
            </button>
          </div>
        </form>

        <p className={styles.footnote}>Données publiques de l’API GitHub · Rien n’est stocké.</p>
      </motion.div>
    </main>
  );
}
