import { useCallback, useEffect, useState } from 'react';

import { HomePage } from './pages/HomePage';
import { WrappedPage } from './pages/WrappedPage';
import { availableYears, isValidLogin } from './lib/validation';

interface Selection {
  readonly login: string;
  readonly year: number;
}

/** Lit `?login=…&year=…` pour rendre un Wrapped partageable par lien ; ignore les valeurs invalides. */
function readSelectionFromUrl(): Selection | null {
  const params = new URLSearchParams(window.location.search);
  const login = params.get('login') ?? '';
  const year = Number(params.get('year'));
  if (!isValidLogin(login) || !availableYears().includes(year)) {
    return null;
  }
  return { login, year };
}

export function App() {
  const [selection, setSelection] = useState<Selection | null>(readSelectionFromUrl);

  useEffect(() => {
    const onPopState = () => setSelection(readSelectionFromUrl());
    window.addEventListener('popstate', onPopState);
    return () => window.removeEventListener('popstate', onPopState);
  }, []);

  const open = useCallback((login: string, year: number) => {
    const params = new URLSearchParams({ login, year: String(year) });
    window.history.pushState(null, '', `?${params.toString()}`);
    setSelection({ login, year });
  }, []);

  const close = useCallback(() => {
    window.history.pushState(null, '', window.location.pathname);
    setSelection(null);
  }, []);

  if (selection === null) {
    return <HomePage onSubmit={open} />;
  }
  return <WrappedPage key={`${selection.login}-${selection.year}`} {...selection} onClose={close} />;
}
