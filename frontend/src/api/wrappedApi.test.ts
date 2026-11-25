import { ApiError, fetchWrapped } from './wrappedApi';
import { makeWrapped } from '../test/fixtures';

function jsonResponse(body: unknown, status: number, headers: Record<string, string> = {}): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': status >= 400 ? 'application/problem+json' : 'application/json', ...headers },
  });
}

describe('fetchWrapped', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('appelle l’endpoint avec login encodé et année', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse(makeWrapped(), 200));
    vi.stubGlobal('fetch', fetchMock);

    const result = await fetchWrapped('octocat', 2025);

    expect(fetchMock).toHaveBeenCalledWith('/api/wrapped/octocat?year=2025', expect.any(Object));
    expect(result.login).toBe('octocat');
  });

  it('transforme un ProblemDetail 404 en ApiError', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ status: 404, detail: 'Utilisateur introuvable.' }, 404)));

    await expect(fetchWrapped('ghost', 2025)).rejects.toMatchObject({
      name: 'ApiError',
      status: 404,
      message: 'Utilisateur introuvable.',
    });
  });

  it('expose le Retry-After d’une réponse 429', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ status: 429 }, 429, { 'Retry-After': '30' })));

    const error = await fetchWrapped('octocat', 2025).catch((e: unknown) => e);

    expect(error).toBeInstanceOf(ApiError);
    expect((error as ApiError).retryAfterSeconds).toBe(30);
  });

  it('signale une erreur réseau de façon générique', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')));

    await expect(fetchWrapped('octocat', 2025)).rejects.toMatchObject({ status: 0 });
  });
});
