import { buildWeeks, contributionLevel, splitInHalves } from './heatmap';

describe('heatmap', () => {
  it('calcule un niveau 0–4 relatif au maximum', () => {
    expect(contributionLevel(0, 10)).toBe(0);
    expect(contributionLevel(1, 10)).toBe(1);
    expect(contributionLevel(5, 10)).toBe(2);
    expect(contributionLevel(10, 10)).toBe(4);
    expect(contributionLevel(3, 0)).toBe(0);
  });

  it('aligne le premier jour sur son jour de semaine (dimanche = ligne 0)', () => {
    // 2025-01-01 est un mercredi → 3 cases vides avant
    const weeks = buildWeeks([
      { date: '2025-01-01', count: 1 },
      { date: '2025-01-02', count: 2 },
    ]);

    expect(weeks).toHaveLength(1);
    expect(weeks[0]?.slice(0, 3)).toEqual([null, null, null]);
    expect(weeks[0]?.[3]).toEqual({ date: '2025-01-01', count: 1 });
    expect(weeks[0]).toHaveLength(7);
  });

  it('coupe les semaines en deux moitiés', () => {
    const weeks = Array.from({ length: 53 }, () => [null, null, null, null, null, null, null]);
    const [first, second] = splitInHalves(weeks);

    expect(first).toHaveLength(27);
    expect(second).toHaveLength(26);
  });
});
