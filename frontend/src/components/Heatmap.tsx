import type { CSSProperties } from 'react';

import type { DayStat } from '../api/types';
import { formatShortDate, formatNumber, pluralize } from '../lib/format';
import { buildWeeks, contributionLevel, splitInHalves, type Week } from '../lib/heatmap';
import styles from './Heatmap.module.css';

interface HeatmapProps {
  readonly days: readonly DayStat[];
}

const LEVEL_CLASS = ['', styles.level1, styles.level2, styles.level3, styles.level4] as const;

function rangeLabel(weeks: readonly Week[]): string {
  const days = weeks.flat().filter((d): d is DayStat => d !== null);
  const first = days[0];
  const last = days[days.length - 1];
  return first && last ? `${formatShortDate(first.date)} → ${formatShortDate(last.date)}` : '';
}

/** Calendrier de contributions qui se remplit case par case (animation CSS échelonnée). */
export function Heatmap({ days }: HeatmapProps) {
  const max = days.reduce((m, d) => Math.max(m, d.count), 0);
  const activeDays = days.filter((d) => d.count > 0).length;
  const halves = splitInHalves(buildWeeks(days)).filter((half) => half.length > 0);

  return (
    <div
      className={styles.heatmap}
      role="img"
      aria-label={`Calendrier des contributions : ${formatNumber(activeDays)} ${pluralize(activeDays, 'jour actif', 'jours actifs')} sur ${formatNumber(days.length)}`}
    >
      {halves.map((weeks, halfIndex) => {
        const weekOffset = halfIndex === 0 ? 0 : (halves[0]?.length ?? 0);
        return (
          <div key={halfIndex} className={styles.half}>
            <span className={styles.label} aria-hidden="true">
              {rangeLabel(weeks)}
            </span>
            <div className={styles.grid}>
              {weeks.flatMap((week, weekIndex) =>
                week.map((day, dayIndex) => {
                  const key = `${weekIndex}-${dayIndex}`;
                  if (day === null) {
                    return <span key={key} className={styles.empty} />;
                  }
                  const level = contributionLevel(day.count, max);
                  const style = { '--week': weekOffset + weekIndex, '--day': dayIndex } as CSSProperties;
                  return (
                    <span
                      key={key}
                      className={`${styles.cell} ${LEVEL_CLASS[level]}`}
                      style={style}
                      data-level={level}
                      title={`${formatShortDate(day.date)} : ${day.count}`}
                    />
                  );
                }),
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}
