import type { ProblemDetail, Wrapped } from './types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export class ApiError extends Error {
  readonly status: number;
  readonly retryAfterSeconds: number | null;

  constructor(status: number, message: string, retryAfterSeconds: number | null = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.retryAfterSeconds = retryAfterSeconds;
  }
}

const FALLBACK_MESSAGES: Record<number, string> = {
  400: 'Requête invalide.',
  404: 'Aucun utilisateur GitHub ne correspond à ce login.',
  429: 'Trop de requêtes. Patiente un peu avant de réessayer.',
  502: 'GitHub ne répond pas pour le moment. Réessaie plus tard.',
};

async function readProblem(response: Response): Promise<ProblemDetail | null> {
  const contentType = response.headers.get('content-type') ?? '';
  if (!contentType.includes('json')) {
    return null;
  }
  try {
    return (await response.json()) as ProblemDetail;
  } catch {
    return null;
  }
}

export async function fetchWrapped(login: string, year: number, signal?: AbortSignal): Promise<Wrapped> {
  const url = `${API_BASE_URL}/api/wrapped/${encodeURIComponent(login)}?year=${encodeURIComponent(year)}`;

  let response: Response;
  try {
    response = await fetch(url, { headers: { Accept: 'application/json' }, ...(signal ? { signal } : {}) });
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw error;
    }
    throw new ApiError(0, 'Impossible de joindre le serveur. Vérifie ta connexion.');
  }

  if (!response.ok) {
    const problem = await readProblem(response);
    const retryAfterHeader = response.headers.get('retry-after');
    const retryAfter = retryAfterHeader !== null && /^\d+$/.test(retryAfterHeader) ? Number(retryAfterHeader) : null;
    // Les messages ProblemDetail sont rédigés par notre backend (jamais de message GitHub brut).
    const message =
      problem?.detail ?? FALLBACK_MESSAGES[response.status] ?? 'Une erreur inattendue est survenue.';
    throw new ApiError(response.status, message, retryAfter);
  }

  return (await response.json()) as Wrapped;
}
