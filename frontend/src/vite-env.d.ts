/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** URL de base du backend en production (vide en dev : proxy Vite). */
  readonly VITE_API_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
