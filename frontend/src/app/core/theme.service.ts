import { Injectable, effect, signal } from '@angular/core';

export type Theme = 'dark' | 'light';

const CLE_STOCKAGE = 'theme-preference';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  readonly theme = signal<Theme>(this.lirePreference());

  constructor() {
    effect(() => {
      const valeur = this.theme();
      document.documentElement.setAttribute('data-theme', valeur);
      try {
        localStorage.setItem(CLE_STOCKAGE, valeur);
      } catch {
        // Stockage indisponible (navigation privée, etc.) : on continue sans persister.
      }
    });
  }

  basculer(): void {
    this.theme.update((valeur) => (valeur === 'dark' ? 'light' : 'dark'));
  }

  private lirePreference(): Theme {
    try {
      const stocke = localStorage.getItem(CLE_STOCKAGE);
      if (stocke === 'light' || stocke === 'dark') {
        return stocke;
      }
    } catch {
      // Stockage indisponible : on retombe sur le sombre par defaut.
    }
    return 'dark';
  }
}
