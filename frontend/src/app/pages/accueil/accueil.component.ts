import { Component, OnInit, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, switchMap } from 'rxjs';
import { LISTE_PAYS } from '../../data/pays';
import { CriteresRecherche, LogementService } from '../../core/logement.service';
import { GeocodageService, SuggestionLieu } from '../../core/geocodage.service';
import { Logement } from '../../models/logement.model';

@Component({
  selector: 'app-accueil',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatIconModule,
    MatAutocompleteModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './accueil.component.html',
  styleUrl: './accueil.component.scss'
})
export class AccueilComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly logementService = inject(LogementService);
  private readonly geocodageService = inject(GeocodageService);

  readonly formulaireRecherche = this.fb.group({
    ville: [''],
    pays: [''],
    dateDebut: [null as Date | null],
    dateFin: [null as Date | null]
  });

  readonly logements = signal<Logement[]>([]);
  readonly chargement = signal(false);

  readonly suggestionsVille = toSignal(
    this.formulaireRecherche.controls.ville.valueChanges.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      switchMap((valeur) => this.geocodageService.rechercherVilles(valeur ?? ''))
    ),
    { initialValue: [] as SuggestionLieu[] }
  );

  readonly suggestionsPays = toSignal(
    this.formulaireRecherche.controls.pays.valueChanges.pipe(
      debounceTime(100),
      switchMap((valeur) => of(this.filtrerPays(valeur ?? '')))
    ),
    { initialValue: LISTE_PAYS }
  );

  ngOnInit(): void {
    this.chargerTout();
  }

  chargerTout(): void {
    this.chargement.set(true);
    this.logementService.lister().subscribe({
      next: (logements) => {
        this.logements.set(logements);
        this.chargement.set(false);
      },
      error: () => this.chargement.set(false)
    });
  }

  rechercher(): void {
    const { ville, pays, dateDebut, dateFin } = this.formulaireRecherche.getRawValue();

    const criteres: CriteresRecherche = {
      ville: ville || undefined,
      pays: pays || undefined,
      dateDebut: dateDebut ? this.formatDate(dateDebut) : undefined,
      dateFin: dateFin ? this.formatDate(dateFin) : undefined
    };

    this.chargement.set(true);
    this.logementService.rechercher(criteres).subscribe({
      next: (logements) => {
        this.logements.set(logements);
        this.chargement.set(false);
      },
      error: () => this.chargement.set(false)
    });
  }

  reinitialiser(): void {
    this.formulaireRecherche.reset();
    this.chargerTout();
  }

  selectionnerVille(evenement: MatAutocompleteSelectedEvent): void {
    const suggestion = evenement.option.value as SuggestionLieu;
    this.formulaireRecherche.controls.ville.setValue(suggestion.ville, { emitEvent: false });
    if (suggestion.pays && !this.formulaireRecherche.controls.pays.value) {
      this.formulaireRecherche.controls.pays.setValue(suggestion.pays);
    }
  }

  afficherVille(suggestion: SuggestionLieu | string): string {
    return typeof suggestion === 'string' ? suggestion : suggestion.ville;
  }

  private filtrerPays(valeur: string): string[] {
    const recherche = valeur.trim().toLowerCase();
    if (!recherche) {
      return LISTE_PAYS;
    }
    return LISTE_PAYS.filter((pays) => pays.toLowerCase().includes(recherche));
  }

  private formatDate(date: Date): string {
    const annee = date.getFullYear();
    const mois = String(date.getMonth() + 1).padStart(2, '0');
    const jour = String(date.getDate()).padStart(2, '0');
    return `${annee}-${mois}-${jour}`;
  }
}
