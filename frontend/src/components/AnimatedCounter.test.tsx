import { render, screen, waitFor } from '@testing-library/react';

import { AnimatedCounter } from './AnimatedCounter';

describe('AnimatedCounter', () => {
  it('expose immédiatement la valeur finale aux technologies d’assistance', () => {
    render(<AnimatedCounter value={1234} />);

    expect(screen.getByText('1 234', { selector: '.sr-only' })).toBeInTheDocument();
  });

  it('affiche la valeur finale formatée une fois l’animation terminée', async () => {
    const { container } = render(<AnimatedCounter value={98765} duration={0.01} />);

    await waitFor(() => {
      expect(container.querySelector('[aria-hidden="true"]')).toHaveTextContent('98 765');
    });
  });

  it('met à jour la cible quand la valeur change', async () => {
    const { container, rerender } = render(<AnimatedCounter value={10} duration={0.01} />);
    rerender(<AnimatedCounter value={42} duration={0.01} />);

    await waitFor(() => {
      expect(container.querySelector('[aria-hidden="true"]')).toHaveTextContent('42');
    });
    expect(screen.getByText('42', { selector: '.sr-only' })).toBeInTheDocument();
  });
});
