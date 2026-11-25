import styles from './ProgressBar.module.css';

interface ProgressBarProps {
  readonly count: number;
  readonly index: number;
  /** Progression (0–1) du segment courant. */
  readonly progress: number;
}

/** Segments façon « stories » : passés pleins, courant partiel, suivants vides. Décoratif. */
export function ProgressBar({ count, index, progress }: ProgressBarProps) {
  return (
    <div className={styles.bar} aria-hidden="true" data-export-ignore="">
      {Array.from({ length: count }, (_, i) => {
        const ratio = i < index ? 1 : i === index ? progress : 0;
        return (
          <div key={i} className={styles.segment} data-testid="progress-segment">
            <div className={styles.fill} style={{ transform: `scaleX(${ratio})` }} />
          </div>
        );
      })}
    </div>
  );
}
