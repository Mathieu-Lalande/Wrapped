import type { Wrapped } from '../api/types';
import { Reveal } from '../components/Reveal';
import { StoryCard } from '../components/StoryCard';
import { WEEKDAY_LABELS, formatNumber, pluralize } from '../lib/format';
import styles from './cards.module.css';

export function PersonaCard({ data }: { readonly data: Wrapped }) {
  const topLanguage = data.topLanguages[0]?.name ?? '—';
  const favoriteDay = data.favoriteWeekday !== null ? WEEKDAY_LABELS[data.favoriteWeekday.day] : '—';

  return (
    <StoryCard theme="aurora" eyebrow={`Ton persona ${data.year}`} login={data.login} year={data.year} exportName="persona">
      <Reveal>
        <p className={styles.lead}>Cette année, tu étais en mode…</p>
      </Reveal>
      <Reveal delay={0.25}>
        <h2 className={`${styles.word} ${styles.gradientText}`}>{data.persona.title}</h2>
      </Reveal>
      <Reveal delay={0.6} as="p" className={styles.note}>
        {data.persona.description}
      </Reveal>
      <Reveal delay={0.9}>
        <dl className={styles.recap}>
          <div className={styles.recapItem}>
            <dt>Contributions</dt>
            <dd>{formatNumber(data.totals.contributions)}</dd>
          </div>
          <div className={styles.recapItem}>
            <dt>Plus longue série</dt>
            <dd>
              {data.longestStreak.days} {pluralize(data.longestStreak.days, 'jour')}
            </dd>
          </div>
          <div className={styles.recapItem}>
            <dt>Langage n°1</dt>
            <dd>{topLanguage}</dd>
          </div>
          <div className={styles.recapItem}>
            <dt>Jour favori</dt>
            <dd>{favoriteDay}</dd>
          </div>
        </dl>
      </Reveal>
    </StoryCard>
  );
}
