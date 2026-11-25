import { fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

import { StoryPlayer, type StoryEntry } from './StoryPlayer';

const entries: StoryEntry[] = [
  { id: 'a', label: 'Première', content: <p>Contenu A</p> },
  { id: 'b', label: 'Deuxième', content: <p>Contenu B</p> },
  { id: 'c', label: 'Troisième', content: <p>Contenu C</p> },
];

function renderPlayer(onClose = vi.fn()) {
  render(<StoryPlayer entries={entries} onClose={onClose} autoAdvanceMs={null} />);
  return { onClose };
}

describe('StoryPlayer', () => {
  it('affiche la première carte et une barre de progression par carte', () => {
    renderPlayer();

    expect(screen.getByText('Contenu A')).toBeInTheDocument();
    expect(screen.getAllByTestId('progress-segment')).toHaveLength(3);
    expect(screen.getByRole('group', { name: '1 sur 3 : Première' })).toBeInTheDocument();
  });

  it('navigue avec les flèches du clavier sans dépasser les bornes', async () => {
    const user = userEvent.setup();
    renderPlayer();

    await user.keyboard('{ArrowLeft}');
    expect(await screen.findByText('Contenu A')).toBeInTheDocument();

    await user.keyboard('{ArrowRight}');
    expect(await screen.findByText('Contenu B')).toBeInTheDocument();

    await user.keyboard('{ArrowRight}{ArrowRight}{ArrowRight}');
    expect(await screen.findByText('Contenu C')).toBeInTheDocument();
    expect(screen.getByText('Carte 3 sur 3 : Troisième')).toBeInTheDocument();

    await user.keyboard('{ArrowLeft}');
    expect(await screen.findByText('Contenu B')).toBeInTheDocument();
  });

  it('avance au tap sur la partie droite et recule au tap sur la partie gauche', async () => {
    renderPlayer();
    const frame = screen.getByTestId('story-frame');
    frame.getBoundingClientRect = () => ({ left: 0, top: 0, width: 300, height: 600, right: 300, bottom: 600, x: 0, y: 0, toJSON: () => ({}) });

    fireEvent.pointerDown(frame, { clientX: 250 });
    fireEvent.pointerUp(frame, { clientX: 250 });
    expect(await screen.findByText('Contenu B')).toBeInTheDocument();

    fireEvent.pointerDown(frame, { clientX: 20 });
    fireEvent.pointerUp(frame, { clientX: 20 });
    expect(await screen.findByText('Contenu A')).toBeInTheDocument();
  });

  it('propose des boutons accessibles précédente / suivante', async () => {
    const user = userEvent.setup();
    renderPlayer();

    expect(screen.getByRole('button', { name: 'Carte précédente' })).toBeDisabled();
    await user.click(screen.getByRole('button', { name: 'Carte suivante' }));

    expect(await screen.findByText('Contenu B')).toBeInTheDocument();
  });

  it('se ferme avec Échap et avec le bouton Fermer', async () => {
    const user = userEvent.setup();
    const { onClose } = renderPlayer();

    await user.keyboard('{Escape}');
    await user.click(screen.getByRole('button', { name: 'Fermer' }));

    expect(onClose).toHaveBeenCalledTimes(2);
  });
});
