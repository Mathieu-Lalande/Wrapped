import { act, renderHook } from '@testing-library/react';

import { useStoryNavigation } from './useStoryNavigation';

describe('useStoryNavigation', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  it('avance automatiquement puis s’arrête sur la dernière carte', () => {
    const { result } = renderHook(() => useStoryNavigation({ count: 2, autoAdvanceMs: 1000 }));

    act(() => vi.advanceTimersByTime(1100));
    expect(result.current.index).toBe(1);

    act(() => vi.advanceTimersByTime(5000));
    expect(result.current.index).toBe(1);
    expect(result.current.progress).toBe(1);
  });

  it('un maintien temporaire ne lève pas la pause choisie par l’utilisateur', () => {
    const { result } = renderHook(() => useStoryNavigation({ count: 3, autoAdvanceMs: 1000 }));

    act(() => result.current.togglePaused());
    act(() => result.current.setHeld(true));
    act(() => result.current.setHeld(false));

    expect(result.current.userPaused).toBe(true);
    expect(result.current.paused).toBe(true);
    act(() => vi.advanceTimersByTime(3000));
    expect(result.current.index).toBe(0);
  });

  it('le maintien suspend l’auto-avance', () => {
    const { result } = renderHook(() => useStoryNavigation({ count: 3, autoAdvanceMs: 1000 }));

    act(() => result.current.setHeld(true));
    act(() => vi.advanceTimersByTime(3000));
    expect(result.current.index).toBe(0);

    act(() => result.current.setHeld(false));
    act(() => vi.advanceTimersByTime(1100));
    expect(result.current.index).toBe(1);
  });
});
