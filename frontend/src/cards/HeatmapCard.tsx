import type { Wrapped } from '../api/types';
import { Heatmap } from '../components/Heatmap';
import { Reveal } from '../components/Reveal';
import { StoryCard } from '../components/StoryCard';
import { MONTH_LABELS, formatNumber, pluralize } from '../lib/format';
import styles from './cards.module.css';

export function HeatmapCard({ data }: { readonly data: Wrapped }) {
  const activeDays = data.calendar.filter((d) => d.count > 0).length;
  const month = data.mostActiveMonth;

  return (
    <StoryCard theme="forest" eyebrow="Ton année, case par case" login={data.login} year={data.year} exportName="calendrier">
      <Reveal>
        <p className={styles.lead}>
          <strong>{formatNumber(activeDays)}</strong> {pluralize(activeDays, 'jour actif', 'jours actifs')} sur{' '}
          {formatNumber(data.calendar.length)}.
        </p>
      </Reveal>
      <Heatmap days={data.calendar} />
      {month !== null && (
        <Reveal delay={1.6} as="p" className={styles.note}>
          Ton mois le plus intense : <strong>{MONTH_LABELS[month.month]}</strong>, avec{' '}
          {formatNumber(month.contributions)} {pluralize(month.contributions, 'contribution')}.
        </Reveal>
      )}
    </StoryCard>
  );
}
