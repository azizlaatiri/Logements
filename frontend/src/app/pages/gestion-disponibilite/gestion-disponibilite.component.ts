import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { DisponibiliteService } from '../../core/disponibilite.service';
import { Calendrier, Indisponibilite } from '../../models/disponibilite.model';

@Component({
  selector: 'app-gestion-disponibilite',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatDatepickerModule,
    MatNativeDateModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './gestion-disponibilite.component.html',
  styleUrl: './gestion-disponibilite.component.scss'
})
export class GestionDisponibiliteComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly disponibiliteService = inject(DisponibiliteService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  readonly logementId = Number(this.route.snapshot.paramMap.get('id'));
  readonly today = new Date();

  readonly chargement = signal(true);
  readonly calendrier = signal<Calendrier | null>(null);
  readonly indisponibilites = signal<Indisponibilite[]>([]);

  readonly recurrenceActivee = signal(false);
  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly formulaire = this.fb.group({
    dateDebut: [null as Date | null, Validators.required],
    dateFin: [null as Date | null, Validators.required],
    motif: [''],
    frequence: ['HEBDOMADAIRE' as 'HEBDOMADAIRE' | 'MENSUELLE'],
    nombreOccurrences: [4, [Validators.min(2), Validators.max(52)]]
  });

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.chargement.set(true);
    this.disponibiliteService.obtenirCalendrier(this.logementId).subscribe({
      next: (calendrier) => this.calendrier.set(calendrier)
    });
    this.disponibiliteService.lister(this.logementId).subscribe({
      next: (indisponibilites) => {
        this.indisponibilites.set(indisponibilites);
        this.chargement.set(false);
      },
      error: () => this.chargement.set(false)
    });
  }

  basculerRecurrence(): void {
    this.recurrenceActivee.update((v) => !v);
  }

  bloquer(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    const { dateDebut, dateFin, motif, frequence, nombreOccurrences } = this.formulaire.getRawValue();
    this.enCours.set(true);
    this.erreur.set(null);

    const requete: Observable<unknown> = this.recurrenceActivee()
      ? this.disponibiliteService.bloquerRecurrent(this.logementId, {
          dateDebut: this.formatDate(dateDebut!),
          dateFin: this.formatDate(dateFin!),
          frequence: frequence!,
          nombreOccurrences: nombreOccurrences!,
          motif: motif || undefined
        })
      : this.disponibiliteService.bloquer(this.logementId, {
          dateDebut: this.formatDate(dateDebut!),
          dateFin: this.formatDate(dateFin!),
          motif: motif || undefined
        });

    requete.subscribe({
      next: () => {
        this.enCours.set(false);
        this.formulaire.reset({ frequence: 'HEBDOMADAIRE', nombreOccurrences: 4 });
        this.snackBar.open('Dates bloquées avec succès', 'Fermer', { duration: 3000 });
        this.charger();
      },
      error: (err: any) => {
        this.enCours.set(false);
        this.erreur.set(err.error?.message ?? 'Erreur lors du blocage des dates');
      }
    });
  }

  debloquer(indisponibilite: Indisponibilite): void {
    this.disponibiliteService.debloquer(this.logementId, indisponibilite.id).subscribe({
      next: () => {
        this.snackBar.open('Blocage supprimé', 'Fermer', { duration: 3000 });
        this.charger();
      },
      error: () => this.snackBar.open('Impossible de supprimer ce blocage', 'Fermer', { duration: 3000 })
    });
  }

  private formatDate(date: Date): string {
    const annee = date.getFullYear();
    const mois = String(date.getMonth() + 1).padStart(2, '0');
    const jour = String(date.getDate()).padStart(2, '0');
    return `${annee}-${mois}-${jour}`;
  }
}
