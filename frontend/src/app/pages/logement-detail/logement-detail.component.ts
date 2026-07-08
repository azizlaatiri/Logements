import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { LogementService } from '../../core/logement.service';
import { ReservationService } from '../../core/reservation.service';
import { Logement } from '../../models/logement.model';

@Component({
  selector: 'app-logement-detail',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './logement-detail.component.html',
  styleUrl: './logement-detail.component.scss'
})
export class LogementDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly logementService = inject(LogementService);
  private readonly reservationService = inject(ReservationService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly logement = signal<Logement | null>(null);
  readonly chargement = signal(true);
  readonly enCoursReservation = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = this.fb.group({
    dateDebut: [null as Date | null, Validators.required],
    dateFin: [null as Date | null, Validators.required]
  });

  private readonly valeursFormulaire = toSignal(this.formulaire.valueChanges, {
    initialValue: this.formulaire.getRawValue()
  });

  readonly nuits = computed(() => {
    const { dateDebut, dateFin } = this.valeursFormulaire();
    if (!dateDebut || !dateFin) {
      return 0;
    }
    const diffMs = new Date(dateFin).setHours(0, 0, 0, 0) - new Date(dateDebut).setHours(0, 0, 0, 0);
    return Math.max(0, Math.round(diffMs / (1000 * 60 * 60 * 24)));
  });

  readonly prixTotal = computed(() => {
    const logement = this.logement();
    return logement ? this.nuits() * logement.prixParNuit : 0;
  });

  readonly galeriePhotos = computed(() => {
    const logement = this.logement();
    if (!logement) {
      return [];
    }
    if (logement.photos && logement.photos.length > 0) {
      return logement.photos;
    }
    return logement.imageUrl ? [logement.imageUrl] : [];
  });

  readonly tailleGalerie = computed(() => Math.min(this.galeriePhotos().length, 5));

  readonly estConnecte = this.authService.estConnecte;
  readonly today = new Date();

  readonly estProprietaire = computed(() => {
    const logement = this.logement();
    const utilisateur = this.authService.utilisateur();
    return !!logement?.proprietaire && !!utilisateur && logement.proprietaire.id === utilisateur.id;
  });

  readonly suppressionEnCours = signal(false);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.logementService.obtenir(id).subscribe({
      next: (logement) => {
        this.logement.set(logement);
        this.chargement.set(false);
      },
      error: () => this.chargement.set(false)
    });
  }

  reserver(): void {
    if (this.formulaire.invalid || !this.logement() || this.estProprietaire()) {
      return;
    }

    if (!this.estConnecte()) {
      this.router.navigate(['/connexion']);
      return;
    }

    const { dateDebut, dateFin } = this.formulaire.getRawValue();
    this.enCoursReservation.set(true);
    this.erreur.set(null);

    this.reservationService
      .reserver(this.logement()!.id, {
        dateDebut: this.formatDate(dateDebut!),
        dateFin: this.formatDate(dateFin!)
      })
      .subscribe({
        next: () => {
          this.enCoursReservation.set(false);
          this.snackBar.open('Réservation confirmée !', 'Fermer', { duration: 4000 });
          this.router.navigate(['/tableau-de-bord']);
        },
        error: (err) => {
          this.enCoursReservation.set(false);
          if (err.status === 409) {
            this.erreur.set("Ce logement n'est pas disponible sur cette période");
          } else if (err.status === 403 && err.error?.message) {
            this.erreur.set(err.error.message);
          } else {
            this.erreur.set('Erreur lors de la réservation');
          }
        }
      });
  }

  modifier(): void {
    const logement = this.logement();
    if (!logement) {
      return;
    }
    this.router.navigate(['/logements', logement.id, 'modifier']);
  }

  supprimer(): void {
    const logement = this.logement();
    if (!logement) {
      return;
    }

    const confirmation = confirm(`Supprimer définitivement "${logement.titre}" ? Cette action est irréversible.`);
    if (!confirmation) {
      return;
    }

    this.suppressionEnCours.set(true);
    this.erreur.set(null);

    this.logementService.supprimer(logement.id).subscribe({
      next: () => {
        this.suppressionEnCours.set(false);
        this.snackBar.open('Logement supprimé', 'Fermer', { duration: 4000 });
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.suppressionEnCours.set(false);
        this.erreur.set(
          err.status === 409
            ? 'Impossible de supprimer ce logement : il a des réservations en cours ou à venir'
            : 'Erreur lors de la suppression du logement'
        );
      }
    });
  }

  private formatDate(date: Date): string {
    const annee = date.getFullYear();
    const mois = String(date.getMonth() + 1).padStart(2, '0');
    const jour = String(date.getDate()).padStart(2, '0');
    return `${annee}-${mois}-${jour}`;
  }
}
