import { Component, OnInit, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { LogementService } from '../../core/logement.service';
import { Logement } from '../../models/logement.model';
import { RevealDirective } from '../../shared/reveal.directive';
import { SpotlightDirective } from '../../shared/spotlight.directive';

@Component({
  selector: 'app-mes-logements',
  standalone: true,
  imports: [RouterLink, MatIconModule, MatProgressSpinnerModule, RevealDirective, SpotlightDirective],
  templateUrl: './mes-logements.component.html',
  styleUrl: './mes-logements.component.scss'
})
export class MesLogementsComponent implements OnInit {
  private readonly logementService = inject(LogementService);

  readonly chargement = signal(true);
  readonly logements = signal<Logement[]>([]);

  ngOnInit(): void {
    this.logementService.mesLogements().subscribe({
      next: (logements) => {
        this.logements.set(logements);
        this.chargement.set(false);
      },
      error: () => this.chargement.set(false)
    });
  }
}
