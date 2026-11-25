import type { Wrapped } from '../api/types';
import { AnimatedCounter } from '../components/AnimatedCounter';
import { Reveal } from '../components/Reveal';
import { StoryCard } from '../components/StoryCard';
import { formatNumber, pluralize } from '../lib/format';
import styles from './cards.module.css';

export function TotalCard({ data }: { readonly data: Wrapped }) {
  const { totals, calendar } = data;
  const perDay = calendar.length > 0 ? totals.contributions / calendar.length : 0;

  return (
    <StoryCard theme="violet" eyebrow={`GitHub Wrapped ${data.year}`} login={data.login} year={data.year} exportName="total">
      <Reveal>
        <p className={styles.lead}>
          {data.name ?? `@${data.login}`}, en {data.year}, tu as cumulé
        </p>
      </Reveal>
      <Reveal delay={0.15}>
        <AnimatedCounter value={totals.contributions} className={styles.giant} delay={0.2} />
        <p className={styles.unit}>{pluralize(totals.contributions, 'contribution')}</p>
      </Reveal>
      <Reveal delay={1.2}>
        <ul className={styles.chips}>
          <li className={styles.chip}>
            <strong>{formatNumber(totals.commits)}</strong> {pluralize(totals.commits, 'commit')}
          </li>
          <li className={styles.chip}>
            <strong>{formatNumber(totals.pullRequests)}</strong> {pluralize(totals.pullRequests, 'pull request')}
          </li>
          <li className={styles.chip}>
            <strong>{formatNumber(totals.issues)}</strong> {pluralize(totals.issues, 'issue')}
          </li>
        </ul>
      </Reveal>
      {totals.contributions > 0 && (
        <Reveal delay={1.5} as="p" className={styles.note}>
          Soit environ <strong>{perDay.toLocaleString('fr-FR', { maximumFractionDigits: 1 })}</strong> par jour.
        </Reveal>
      )}
    </StoryCard>
  );
}
