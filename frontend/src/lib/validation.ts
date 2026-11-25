/** Même règle que le backend (@Pattern sur le login). */
export const GITHUB_LOGIN_PATTERN = /^[a-zA-Z0-9-]{1,39}$/;

export const FIRST_YEAR = 2008;

export function isValidLogin(login: string): boolean {
  return GITHUB_LOGIN_PATTERN.test(login);
}

/** Années disponibles, de la plus récente à 2008 (année courante en UTC, comme le backend). */
export function availableYears(now: Date = new Date()): number[] {
  const current = now.getUTCFullYear();
  return Array.from({ length: current - FIRST_YEAR + 1 }, (_, i) => current - i);
}
