import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-etoiles',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './etoiles.component.html',
  styleUrl: './etoiles.component.scss'
})
export class EtoilesComponent {
  /** En lecture seule peut être fractionnaire (ex: moyenne 4.3). En mode interactif, entier 0-5. */
  @Input() note = 0;
  @Input() interactif = false;
  @Input() taille: 'petite' | 'normale' = 'normale';
  @Output() noteChange = new EventEmitter<number>();

  readonly survol = signal(0);
  readonly positions = [1, 2, 3, 4, 5];

  icone(position: number): string {
    const reference = this.interactif && this.survol() ? this.survol() : this.note;
    const diff = reference - (position - 1);
    if (diff >= 1) {
      return 'star';
    }
    if (diff >= 0.5) {
      return 'star_half';
    }
    return 'star_border';
  }

  choisir(valeur: number): void {
    if (!this.interactif) {
      return;
    }
    this.note = valeur;
    this.noteChange.emit(valeur);
  }

  survoler(valeur: number): void {
    if (this.interactif) {
      this.survol.set(valeur);
    }
  }

  arreterSurvol(): void {
    this.survol.set(0);
  }
}
