import { useRef, type ReactNode } from 'react';

import { useExportPng } from '../hooks/useExportPng';
import { ShareButton } from './ShareButton';
import { useStoryControls } from './StoryControls';
import styles from './StoryCard.module.css';

export type CardTheme = 'violet' | 'forest' | 'ember' | 'ocean' | 'midnight' | 'sun' | 'aurora';

interface StoryCardProps {
  readonly theme: CardTheme;
  readonly eyebrow: string;
  readonly login: string;
  readonly year: number;
  /** Suffixe du nom de fichier exporté, ex. "streak". */
  readonly exportName: string;
  readonly children: ReactNode;
}

/**
 * Carte plein écran : fond thématique, contenu centré, pied de page signé
 * (inclus dans l'image) et bouton de partage (exclu de l'image).
 */
export function StoryCard({ theme, eyebrow, login, year, exportName, children }: StoryCardProps) {
  const cardRef = useRef<HTMLElement>(null);
  const { status, exportNode } = useExportPng();
  const { setHeld } = useStoryControls();

  const handleShare = async () => {
    if (cardRef.current === null) {
      return;
    }
    setHeld(true);
    try {
      await exportNode(cardRef.current, `github-wrapped-${login}-${year}-${exportName}.png`, `GitHub Wrapped ${year}`);
    } finally {
      setHeld(false);
    }
  };

  return (
    <article ref={cardRef} className={`${styles.card} ${styles[theme]}`}>
      <p className={styles.eyebrow}>{eyebrow}</p>
      <div className={styles.body}>{children}</div>
      <footer className={styles.footer}>
        <span className={styles.brand}>
          <strong>GitHub Wrapped</strong> · {year} · @{login}
        </span>
        <ShareButton status={status} onShare={() => void handleShare()} />
      </footer>
    </article>
  );
}
