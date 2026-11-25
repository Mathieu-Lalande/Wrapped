import { useCallback, useEffect, useMemo, useRef, type PointerEvent, type ReactNode } from 'react';
import { AnimatePresence, motion, type Variants } from 'framer-motion';

import { useStoryNavigation } from '../hooks/useStoryNavigation';
import { ProgressBar } from './ProgressBar';
import { StoryControlsContext } from './StoryControls';
import styles from './StoryPlayer.module.css';

export interface StoryEntry {
  readonly id: string;
  /** Titre court, annoncé aux lecteurs d'écran. */
  readonly label: string;
  readonly content: ReactNode;
}

interface StoryPlayerProps {
  readonly entries: readonly StoryEntry[];
  readonly onClose: () => void;
  /** `null` désactive l'auto-avance. */
  readonly autoAdvanceMs?: number | null;
}

/** En dessous de ce délai, un appui est un « tap » (navigation) ; au-delà, un appui long (pause). */
const TAP_MAX_MS = 250;

const slideVariants: Variants = {
  enter: (direction: number) => ({ opacity: 0, x: direction * 60, scale: 0.96 }),
  center: { opacity: 1, x: 0, scale: 1 },
  exit: (direction: number) => ({ opacity: 0, x: direction * -60, scale: 0.96 }),
};

function isInteractive(target: EventTarget | null): boolean {
  return target instanceof Element && target.closest('button, a, input, select, textarea') !== null;
}

/**
 * Lecteur de stories plein écran.
 * Navigation : tap à droite/gauche, appui long = pause, flèches clavier, Espace = pause, Échap = fermer.
 */
export function StoryPlayer({ entries, onClose, autoAdvanceMs = 8000 }: StoryPlayerProps) {
  const nav = useStoryNavigation({ count: entries.length, autoAdvanceMs });
  const { next, previous, setHeld, togglePaused } = nav;
  const pressStartRef = useRef<number | null>(null);
  const entry = entries[nav.index];

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.defaultPrevented || event.altKey || event.ctrlKey || event.metaKey) {
        return;
      }
      switch (event.key) {
        case 'ArrowRight':
          event.preventDefault();
          next();
          break;
        case 'ArrowLeft':
          event.preventDefault();
          previous();
          break;
        case ' ':
          // Sur un bouton focalisé, Espace doit garder son comportement natif (activer le bouton).
          if (!isInteractive(event.target)) {
            event.preventDefault();
            togglePaused();
          }
          break;
        case 'Escape':
          onClose();
          break;
        default:
          break;
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [next, previous, togglePaused, onClose]);

  const handlePointerDown = (event: PointerEvent<HTMLDivElement>) => {
    if (isInteractive(event.target)) {
      return;
    }
    pressStartRef.current = event.timeStamp;
    setHeld(true);
  };

  const handlePointerUp = (event: PointerEvent<HTMLDivElement>) => {
    const start = pressStartRef.current;
    pressStartRef.current = null;
    if (start === null) {
      return;
    }
    setHeld(false);
    if (event.timeStamp - start > TAP_MAX_MS) {
      return; // appui long : simple pause, pas de navigation
    }
    const bounds = event.currentTarget.getBoundingClientRect();
    const isLeftZone = event.clientX - bounds.left < bounds.width / 3;
    if (isLeftZone) {
      previous();
    } else {
      next();
    }
  };

  const handlePointerCancel = () => {
    // Ne relance que si c'est un appui en cours qui avait mis en pause.
    if (pressStartRef.current !== null) {
      pressStartRef.current = null;
      setHeld(false);
    }
  };

  const controls = useMemo(() => ({ setHeld }), [setHeld]);
  const stopPropagation = useCallback((event: { stopPropagation: () => void }) => event.stopPropagation(), []);

  if (entry === undefined) {
    return null;
  }

  return (
    <div className={styles.player} role="region" aria-roledescription="carrousel" aria-label="Ton GitHub Wrapped">
      <div
        className={styles.frame}
        onPointerDown={handlePointerDown}
        onPointerUp={handlePointerUp}
        onPointerCancel={handlePointerCancel}
        onPointerLeave={handlePointerCancel}
        data-testid="story-frame"
      >
        <ProgressBar count={entries.length} index={nav.index} progress={nav.progress} />

        <div className={styles.topbar} data-export-ignore="">
          {autoAdvanceMs !== null && (
            <button
              type="button"
              className={styles.iconButton}
              onClick={togglePaused}
              onPointerDown={stopPropagation}
              aria-label={nav.userPaused ? 'Reprendre la lecture' : 'Mettre en pause'}
            >
              {nav.userPaused ? (
                <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><path d="M8 5.5v13l10.5-6.5z" /></svg>
              ) : (
                <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden="true"><rect x="6.5" y="5" width="4" height="14" rx="1" /><rect x="13.5" y="5" width="4" height="14" rx="1" /></svg>
              )}
            </button>
          )}
          <button type="button" className={styles.iconButton} onClick={onClose} onPointerDown={stopPropagation} aria-label="Fermer">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18" /></svg>
          </button>
        </div>

        <StoryControlsContext.Provider value={controls}>
          <AnimatePresence mode="wait" initial={false} custom={nav.direction}>
            <motion.div
              key={entry.id}
              className={styles.slide}
              custom={nav.direction}
              variants={slideVariants}
              initial="enter"
              animate="center"
              exit="exit"
              transition={{ duration: 0.35, ease: [0.16, 1, 0.3, 1] }}
              role="group"
              aria-roledescription="diapositive"
              aria-label={`${nav.index + 1} sur ${entries.length} : ${entry.label}`}
            >
              {entry.content}
            </motion.div>
          </AnimatePresence>
        </StoryControlsContext.Provider>
      </div>

      <button
        type="button"
        className={`${styles.nav} ${styles.prev}`}
        onClick={previous}
        disabled={nav.index === 0}
        aria-label="Carte précédente"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M15 5l-7 7 7 7" /></svg>
      </button>
      <button
        type="button"
        className={`${styles.nav} ${styles.next}`}
        onClick={next}
        disabled={nav.index === entries.length - 1}
        aria-label="Carte suivante"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M9 5l7 7-7 7" /></svg>
      </button>

      <p className="sr-only" aria-live="polite">
        Carte {nav.index + 1} sur {entries.length} : {entry.label}
      </p>
    </div>
  );
}
