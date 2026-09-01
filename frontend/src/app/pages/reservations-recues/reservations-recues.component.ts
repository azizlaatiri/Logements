import { Component, OnInit, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReservationService } from '../../core/reservation.service';
import { Reservation, StatutPaiement } from '../../models/reservation.model';
import { RevealDirective } from '../../shared/reveal.directive';

const LIBELLES_PAIEMENT: Record<StatutPaiement, string> = {
  NON_PAYE: 'Paiement en attente',
  PAYE: 'Payé',
  REMBOURSE: 'Remboursé'
};

@Component({
  selector: 'app-reservations-recues',
  standalone: true,
  imports: [RouterLink, MatIconModule, MatProgressSpinnerModule, RevealDirective],
  templateUrl: './reservations-recues.component.html',
  styleUrl: './reservations-recues.component.scss'
})
export class ReservationsRecuesComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly reservationService = inject(ReservationService);
  private readonly snackBar = inject(MatSnackBar);

  readonly logementId = Number(this.route.snapshot.paramMap.get('id'));

  readonly chargement = signal(true);
  readonly reservations = signal<Reservation[]>([]);

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement.set(true);
    this.reservationService.reservationsRecues(this.logementId).subscribe({
      next: (reservations) => {
        this.reservations.set(reservations);
        this.chargement.set(false);
      },
      error: () => this.chargement.set(false)
    });
  }

  libellePaiement(statut: StatutPaiement): string {
    return LIBELLES_PAIEMENT[statut];
  }

  annuler(reservation: Reservation): void {
    this.reservationService.annuler(reservation.id).subscribe({
      next: (res) => {
        const message = res.statutPaiement === 'REMBOURSE' ? 'Réservation annulée et remboursée' : 'Réservation annulée';
        this.snackBar.open(message, 'Fermer', { duration: 3000 });
        this.charger();
      },
      error: () => this.snackBar.open("Impossible d'annuler cette réservation", 'Fermer', { duration: 3000 })
    });
  }

  confirmer(reservation: Reservation): void {
    this.reservationService.confirmer(reservation.id).subscribe({
      next: () => {
        this.snackBar.open('Réservation confirmée', 'Fermer', { duration: 3000 });
        this.charger();
      },
      error: () => this.snackBar.open("Impossible de confirmer cette réservation", 'Fermer', { duration: 3000 })
    });
  }
}
