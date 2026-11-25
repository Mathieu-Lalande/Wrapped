import { motion } from 'framer-motion';

import type { LanguageStat } from '../api/types';
import { formatNumber, pluralize } from '../lib/format';
import styles from './LanguageBar.module.css';

/** Couleur de repli quand GitHub ne fournit pas de couleur de langage. */
export const FALLBACK_LANGUAGE_COLOR = '#c9c3d6';

const HEX_COLOR = /^#[0-9a-fA-F]{3,8}$/;

/** N'accepte que des couleurs hexadécimales (évite toute injection de valeur CSS arbitraire). */
export function safeColor(color: string | null): string {
  return color !== null && HEX_COLOR.test(color) ? color : FALLBACK_LANGUAGE_COLOR;
}

interface LanguageBarProps {
  readonly languages: readonly LanguageStat[];
}

/** Barres horizontales aux couleurs GitHub de chaque langage. */
export function LanguageBar({ languages }: LanguageBarProps) {
  return (
    <ul className={styles.list}>
      {languages.map((language, i) => {
        const color = safeColor(language.color);
        return (
          <li key={language.name} className={styles.row}>
            <div className={styles.header}>
              <span className={styles.name}>
                <span className={styles.dot} style={{ background: color }} aria-hidden="true" />
                {language.name}
              </span>
              <span className={styles.percent}>
                {language.percentage.toLocaleString('fr-FR')} %
                <span className="sr-only">
                  , {formatNumber(language.commits)} {pluralize(language.commits, 'commit')}
                </span>
              </span>
            </div>
            <div className={styles.track} aria-hidden="true">
              <motion.div
                className={styles.fill}
                style={{ background: color }}
                initial={{ width: 0 }}
                animate={{ width: `${language.percentage}%` }}
                transition={{ duration: 1, delay: 0.3 + i * 0.12, ease: [0.16, 1, 0.3, 1] }}
              />
            </div>
          </li>
        );
      })}
    </ul>
  );
}
