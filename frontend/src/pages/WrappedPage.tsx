import { useMemo } from 'react';

import { buildStory } from '../cards/buildStory';
import { StoryPlayer } from '../components/StoryPlayer';
import { useWrapped } from '../hooks/useWrapped';
import { ErrorScreen, LoadingScreen } from './StatusScreen';

interface WrappedPageProps {
  readonly login: string;
  readonly year: number;
  readonly onClose: () => void;
}

export function WrappedPage({ login, year, onClose }: WrappedPageProps) {
  const state = useWrapped(login, year);
  const entries = useMemo(() => (state.status === 'success' ? buildStory(state.data) : []), [state]);

  switch (state.status) {
    case 'loading':
      return <LoadingScreen login={login} />;
    case 'error':
      return <ErrorScreen error={state.error} onRetry={state.reload} onBack={onClose} />;
    case 'success':
      return <StoryPlayer entries={entries} onClose={onClose} />;
  }
}
