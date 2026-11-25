import { createContext, useContext } from 'react';

interface StoryControls {
  /** Suspend temporairement l'auto-avance (ex. pendant un export PNG) sans toucher à la pause utilisateur. */
  readonly setHeld: (held: boolean) => void;
}

const noop: StoryControls = { setHeld: () => undefined };

export const StoryControlsContext = createContext<StoryControls>(noop);

export function useStoryControls(): StoryControls {
  return useContext(StoryControlsContext);
}
