import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';

import { HomePage } from './HomePage';

describe('HomePage', () => {
  it('refuse un login invalide et affiche une erreur accessible', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<HomePage onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText('Login GitHub'), 'bad_login!');
    await user.click(screen.getByRole('button', { name: /lancer mon wrapped/i }));

    expect(screen.getByRole('alert')).toHaveTextContent(/login invalide/i);
    expect(screen.getByLabelText('Login GitHub')).toHaveAttribute('aria-invalid', 'true');
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('soumet le login normalisé (sans @ ni espaces) et l’année choisie', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<HomePage onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText('Login GitHub'), '  @octo-cat ');
    await user.selectOptions(screen.getByLabelText('Année'), '2020');
    await user.click(screen.getByRole('button', { name: /lancer mon wrapped/i }));

    expect(onSubmit).toHaveBeenCalledWith('octo-cat', 2020);
  });

  it('propose les années de 2008 à l’année courante', () => {
    render(<HomePage onSubmit={vi.fn()} />);

    const options = screen.getAllByRole('option').map((o) => Number(o.textContent));
    expect(options[0]).toBe(new Date().getUTCFullYear());
    expect(options.at(-1)).toBe(2008);
  });
});
