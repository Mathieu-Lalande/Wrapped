import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import '@fontsource-variable/bricolage-grotesque';
import '@fontsource-variable/jetbrains-mono';
import './styles/global.css';

import { App } from './App';

const container = document.getElementById('root');
if (container === null) {
  throw new Error('Élément #root introuvable');
}

createRoot(container).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
