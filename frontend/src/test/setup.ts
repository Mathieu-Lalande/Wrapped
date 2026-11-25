import '@testing-library/jest-dom/vitest';
import { MotionGlobalConfig } from 'framer-motion';

// Animations instantanées : les tests vérifient l'état final, pas les transitions.
MotionGlobalConfig.skipAnimations = true;

// jsdom n'implémente pas matchMedia (utilisé par useReducedMotion).
if (typeof window.matchMedia !== 'function') {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: (query: string): MediaQueryList =>
      ({
        matches: false,
        media: query,
        onchange: null,
        addListener: () => undefined,
        removeListener: () => undefined,
        addEventListener: () => undefined,
        removeEventListener: () => undefined,
        dispatchEvent: () => false,
      }) as MediaQueryList,
  });
}
