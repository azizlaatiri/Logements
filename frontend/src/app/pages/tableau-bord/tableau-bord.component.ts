import { Component, OnInit, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReservationService } from '../../core/reservation.service';
import { Reservation, StatutPaiement } from '../../models/reservation.model';
import { RevealDirective } from '../../shared/reveal.directive';

const LIBELLES_PAIEMENT: Record<StatutPaiement, string> = {
  NON_PAYE: 'Paiement requis',
  PAYE: 'Payé',
  REMBOURSE: 'Remboursé'
};

@Component({
  selector: 'app-tableau-bord',
  standalone: true,
  imports: [RouterLink, MatIconModule, MatProgressSpinnerModule, RevealDirective],
  templateUrl: './tableau-bord.component.html',
  styleUrl: './tableau-bord.component.scss'
})
export class TableauBordComponent implements OnInit {
  readonly reservations = signal<Reservation[]>([]);
  readonly chargement = signal(false);
  readonly paiementEnCours = signal<number | null>(null);

  constructor(
    private reservationService: ReservationService,
    private snackBar: MatSnackBar,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.charger();

    const paiement = this.route.snapshot.queryParamMap.get('paiement');
    if (paiement === 'succes') {
      this.snackBar.open('Paiement effectué, merci !', 'Fermer', { duration: 4000 });
    } else if (paiement === 'annule') {
      this.snackBar.open('Paiement annulé', 'Fermer', { duration: 4000 });
    }
    if (paiement) {
      this.router.navigate([], { relativeTo: this.route, queryParams: {}, replaceUrl: true });
    }
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
      next: (res) => {
        const message = res.statutPaiement === 'REMBOURSE' ? 'Réservation annulée et remboursée' : 'Réservation annulée';
        this.snackBar.open(message, 'Fermer', { duration: 3000 });
        this.charger();
      },
      error: () => this.snackBar.open("Impossible d'annuler cette réservation", 'Fermer', { duration: 3000 })
    });
  }

  libellePaiement(statut: StatutPaiement): string {
    return LIBELLES_PAIEMENT[statut];
  }

  payer(reservation: Reservation): void {
    this.paiementEnCours.set(reservation.id);
    this.reservationService.payer(reservation.id).subscribe({
      next: ({ url }) => {
        window.location.href = url;
      },
      error: () => {
        this.paiementEnCours.set(null);
        this.snackBar.open('Impossible de démarrer le paiement', 'Fermer', { duration: 3000 });
      }
    });
  }
}
