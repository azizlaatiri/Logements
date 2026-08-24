import { Component, OnInit, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReservationService } from '../../core/reservation.service';
import { Reservation } from '../../models/reservation.model';

@Component({
  selector: 'app-reservations-recues',
  standalone: true,
  imports: [RouterLink, MatIconModule, MatProgressSpinnerModule],
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

  annuler(reservation: Reservation): void {
    this.reservationService.annuler(reservation.id).subscribe({
      next: () => {
        this.snackBar.open('Réservation annulée', 'Fermer', { duration: 3000 });
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
