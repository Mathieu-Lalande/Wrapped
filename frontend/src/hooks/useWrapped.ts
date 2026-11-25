import { useEffect, useState } from 'react';

import type { Wrapped } from '../api/types';
import { ApiError, fetchWrapped } from '../api/wrappedApi';

export type WrappedState =
  | { readonly status: 'loading' }
  | { readonly status: 'success'; readonly data: Wrapped }
  | { readonly status: 'error'; readonly error: ApiError };

/**
 * Charge le Wrapped d'un utilisateur. Annule la requête en cours si les paramètres changent
 * ou si le composant est démonté. {@code reload} relance la requête.
 */
export function useWrapped(login: string, year: number): WrappedState & { reload: () => void } {
  const [state, setState] = useState<WrappedState>({ status: 'loading' });
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setState({ status: 'loading' });

    fetchWrapped(login, year, controller.signal)
      .then((data) => setState({ status: 'success', data }))
      .catch((error: unknown) => {
        if (controller.signal.aborted) {
          return;
        }
        const apiError =
          error instanceof ApiError ? error : new ApiError(0, 'Une erreur inattendue est survenue.');
        setState({ status: 'error', error: apiError });
      });

    return () => controller.abort();
  }, [login, year, attempt]);

  return { ...state, reload: () => setAttempt((n) => n + 1) };
}
