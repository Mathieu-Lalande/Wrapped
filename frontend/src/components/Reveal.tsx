import type { ReactNode } from 'react';
import { motion } from 'framer-motion';

interface RevealProps {
  readonly children: ReactNode;
  /** Délai d'apparition en secondes. */
  readonly delay?: number;
  readonly className?: string | undefined;
  readonly as?: 'div' | 'p' | 'h2' | 'li';
}

/** Apparition « fondu + montée », brique commune des cartes. */
export function Reveal({ children, delay = 0, className, as = 'div' }: RevealProps) {
  const Component = motion[as];
  return (
    <Component
      className={className}
      initial={{ opacity: 0, y: 24 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.7, delay, ease: [0.16, 1, 0.3, 1] }}
    >
      {children}
    </Component>
  );
}
