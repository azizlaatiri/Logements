import { Component, OnInit, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { ReservationService } from '../../core/reservation.service';
import { Reservation } from '../../models/reservation.model';

@Component({
  selector: 'app-tableau-bord',
  standalone: true,
  imports: [RouterLink, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './tableau-bord.component.html',
  styleUrl: './tableau-bord.component.scss'
})
export class TableauBordComponent implements OnInit {
  readonly reservations = signal<Reservation[]>([]);
  readonly chargement = signal(false);

  constructor(private reservationService: ReservationService, private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement.set(true);
    this.reservationService.mesReservations().subscribe({
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
}
