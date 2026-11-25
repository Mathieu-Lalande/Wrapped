import { useCallback, useEffect, useRef, useState } from 'react';

interface Options {
  readonly count: number;
  /** Durée d'affichage d'une carte avant passage automatique ; `null` désactive l'auto-avance. */
  readonly autoAdvanceMs: number | null;
}

export interface StoryNavigation {
  readonly index: number;
  /** Sens de la dernière navigation, utilisé pour orienter la transition. */
  readonly direction: 1 | -1;
  /** Pause effective : choisie par l'utilisateur ou maintenue temporairement (appui long, export…). */
  readonly paused: boolean;
  /** Pause choisie explicitement par l'utilisateur (bouton, Espace). */
  readonly userPaused: boolean;
  /** Progression (0–1) de la carte courante. */
  readonly progress: number;
  next: () => void;
  previous: () => void;
  goTo: (index: number) => void;
  /** Maintien temporaire, indépendant de la pause utilisateur (qu'il ne modifie jamais). */
  setHeld: (held: boolean) => void;
  togglePaused: () => void;
}

/**
 * État de navigation d'un lecteur de stories : index courant, sens de transition,
 * auto-avance avec pause. L'auto-avance s'arrête sur la dernière carte.
 */
export function useStoryNavigation({ count, autoAdvanceMs }: Options): StoryNavigation {
  const [index, setIndex] = useState(0);
  const [direction, setDirection] = useState<1 | -1>(1);
  const [userPaused, setUserPaused] = useState(false);
  const [held, setHeld] = useState(false);
  const paused = userPaused || held;
  const [progress, setProgress] = useState(0);
  const elapsedRef = useRef(0);

  const goTo = useCallback(
    (target: number) => {
      const clamped = Math.max(0, Math.min(count - 1, target));
      if (clamped === index) {
        return;
      }
      setDirection(clamped > index ? 1 : -1);
      setIndex(clamped);
      elapsedRef.current = 0;
      setProgress(0);
    },
    [count, index],
  );

  const next = useCallback(() => goTo(index + 1), [goTo, index]);
  const previous = useCallback(() => goTo(index - 1), [goTo, index]);
  const togglePaused = useCallback(() => setUserPaused((p) => !p), []);

  const isLast = index >= count - 1;

  useEffect(() => {
    if (autoAdvanceMs === null || paused || elapsedRef.current >= autoAdvanceMs) {
      return undefined;
    }
    let frame = 0;
    let last = performance.now();
    const tick = (now: number) => {
      elapsedRef.current += now - last;
      last = now;
      const ratio = Math.min(1, elapsedRef.current / autoAdvanceMs);
      setProgress(ratio);
      if (ratio >= 1) {
        if (!isLast) {
          next();
        }
        return;
      }
      frame = requestAnimationFrame(tick);
    };
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [autoAdvanceMs, paused, isLast, next]);

  return {
    index,
    direction,
    paused,
    userPaused,
    progress: autoAdvanceMs === null ? 1 : progress,
    next,
    previous,
    goTo,
    setHeld,
    togglePaused,
  };
}
