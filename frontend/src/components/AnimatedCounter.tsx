import { useEffect, useState } from 'react';
import { animate, useReducedMotion } from 'framer-motion';

import { formatNumber } from '../lib/format';

interface AnimatedCounterProps {
  readonly value: number;
  /** Durée en secondes. */
  readonly duration?: number;
  readonly delay?: number;
  readonly className?: string | undefined;
}

/**
 * Compteur qui défile de 0 à `value`. Les lecteurs d'écran ne lisent que la valeur finale ;
 * l'animation est désactivée si l'utilisateur préfère réduire les mouvements.
 */
export function AnimatedCounter({ value, duration = 1.6, delay = 0, className }: AnimatedCounterProps) {
  const reduceMotion = useReducedMotion() ?? false;
  const [displayed, setDisplayed] = useState(reduceMotion ? value : 0);

  useEffect(() => {
    if (reduceMotion) {
      setDisplayed(value);
      return undefined;
    }
    const controls = animate(0, value, {
      duration,
      delay,
      ease: [0.16, 1, 0.3, 1],
      onUpdate: (latest) => setDisplayed(Math.round(latest)),
    });
    return () => controls.stop();
  }, [value, duration, delay, reduceMotion]);

  return (
    <span className={className} style={{ fontVariantNumeric: 'tabular-nums' }}>
      <span aria-hidden="true">{formatNumber(displayed)}</span>
      <span className="sr-only">{formatNumber(value)}</span>
    </span>
  );
}
