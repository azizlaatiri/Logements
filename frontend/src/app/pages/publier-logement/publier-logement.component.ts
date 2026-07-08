import { Component, OnInit, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, forkJoin, of, switchMap } from 'rxjs';
import { LISTE_PAYS } from '../../data/pays';
import { FichierService } from '../../core/fichier.service';
import { GeocodageService, SuggestionLieu } from '../../core/geocodage.service';
import { LogementService } from '../../core/logement.service';

@Component({
  selector: 'app-publier-logement',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatAutocompleteModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './publier-logement.component.html',
  styleUrl: './publier-logement.component.scss'
})
export class PublierLogementComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly logementService = inject(LogementService);
  private readonly fichierService = inject(FichierService);
  private readonly geocodageService = inject(GeocodageService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly formulaire = this.fb.group({
    titre: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(100)]],
    ville: ['', [Validators.required, Validators.minLength(2)]],
    adresse: [''],
    pays: ['', Validators.required],
    description: ['', Validators.maxLength(2000)],
    prixParNuit: [null as number | null, [Validators.required, Validators.min(1), Validators.max(100000)]],
    nombreChambres: [null as number | null, [Validators.min(1), Validators.max(50)]],
    nombreVoyageursMax: [null as number | null, [Validators.min(1), Validators.max(50)]],
    imageUrl: ['']
  });

  readonly photosCatalogue = signal<string[]>([]);

  readonly logementId = signal<number | null>(null);
  readonly modeEdition = signal(false);
  readonly chargementInitial = signal(false);

  readonly enCoursCouverture = signal(false);
  readonly enCoursCatalogue = signal(false);
  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  readonly suggestionsVille = toSignal(
    this.formulaire.controls.ville.valueChanges.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      switchMap((valeur) => this.geocodageService.rechercherVilles(valeur ?? ''))
    ),
    { initialValue: [] as SuggestionLieu[] }
  );

  readonly suggestionsAdresse = toSignal(
    this.formulaire.controls.adresse.valueChanges.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      switchMap((valeur) => this.geocodageService.rechercherAdresses(valeur ?? ''))
    ),
    { initialValue: [] as SuggestionLieu[] }
  );

  readonly suggestionsPays = toSignal(
    this.formulaire.controls.pays.valueChanges.pipe(
      debounceTime(100),
      switchMap((valeur) => of(this.filtrerPays(valeur ?? '')))
    ),
    { initialValue: LISTE_PAYS }
  );

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      return;
    }

    const id = Number(idParam);
    this.logementId.set(id);
    this.modeEdition.set(true);
    this.chargementInitial.set(true);

    this.logementService.obtenir(id).subscribe({
      next: (logement) => {
        this.formulaire.patchValue({
          titre: logement.titre,
          ville: logement.ville,
          adresse: logement.adresse ?? '',
          pays: logement.pays ?? '',
          description: logement.description ?? '',
          prixParNuit: logement.prixParNuit,
          nombreChambres: logement.nombreChambres ?? null,
          nombreVoyageursMax: logement.nombreVoyageursMax ?? null,
          imageUrl: logement.imageUrl ?? ''
        });
        this.photosCatalogue.set(logement.photos ?? []);
        this.chargementInitial.set(false);
      },
      error: () => {
        this.chargementInitial.set(false);
        this.erreur.set('Impossible de charger ce logement');
      }
    });
  }

  private filtrerPays(valeur: string): string[] {
    const recherche = valeur.trim().toLowerCase();
    if (!recherche) {
      return LISTE_PAYS;
    }
    return LISTE_PAYS.filter((pays) => pays.toLowerCase().includes(recherche));
  }

  selectionnerVille(evenement: MatAutocompleteSelectedEvent): void {
    const suggestion = evenement.option.value as SuggestionLieu;
    this.formulaire.controls.ville.setValue(suggestion.ville, { emitEvent: false });
    if (suggestion.pays && !this.formulaire.controls.pays.value) {
      this.formulaire.controls.pays.setValue(suggestion.pays);
    }
  }

  selectionnerAdresse(evenement: MatAutocompleteSelectedEvent): void {
    const suggestion = evenement.option.value as SuggestionLieu;
    this.formulaire.controls.adresse.setValue(suggestion.libelle, { emitEvent: false });
  }

  afficherVille(suggestion: SuggestionLieu | string): string {
    return typeof suggestion === 'string' ? suggestion : suggestion.ville;
  }

  afficherAdresse(suggestion: SuggestionLieu | string): string {
    return typeof suggestion === 'string' ? suggestion : suggestion.libelle;
  }

  selectionCouverture(event: Event): void {
    const fichier = (event.target as HTMLInputElement).files?.[0];
    if (!fichier) {
      return;
    }

    this.enCoursCouverture.set(true);
    this.erreur.set(null);

    this.fichierService.televerser(fichier).subscribe({
      next: ({ url }) => {
        this.formulaire.controls.imageUrl.setValue(url);
        this.enCoursCouverture.set(false);
      },
      error: () => {
        this.enCoursCouverture.set(false);
        this.erreur.set("Erreur lors de l'envoi de la photo de couverture");
      }
    });
  }

  selectionCatalogue(event: Event): void {
    const fichiers = Array.from((event.target as HTMLInputElement).files ?? []);
    if (fichiers.length === 0) {
      return;
    }

    this.enCoursCatalogue.set(true);
    this.erreur.set(null);

    forkJoin(fichiers.map((fichier) => this.fichierService.televerser(fichier))).subscribe({
      next: (resultats) => {
        this.photosCatalogue.update((photos) => [...photos, ...resultats.map((r) => r.url)]);
        this.enCoursCatalogue.set(false);
      },
      error: () => {
        this.enCoursCatalogue.set(false);
        this.erreur.set("Erreur lors de l'envoi d'une ou plusieurs photos");
      }
    });

    (event.target as HTMLInputElement).value = '';
  }

  supprimerPhoto(index: number): void {
    this.photosCatalogue.update((photos) => photos.filter((_, i) => i !== index));
  }

  soumettre(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    this.enCours.set(true);
    this.erreur.set(null);

    const valeurs = this.formulaire.getRawValue();
    const donnees = {
      titre: valeurs.titre!,
      ville: valeurs.ville!,
      adresse: valeurs.adresse || undefined,
      pays: valeurs.pays!,
      description: valeurs.description || undefined,
      prixParNuit: valeurs.prixParNuit!,
      nombreChambres: valeurs.nombreChambres ?? undefined,
      nombreVoyageursMax: valeurs.nombreVoyageursMax ?? undefined,
      imageUrl: valeurs.imageUrl || undefined,
      photos: this.photosCatalogue().length > 0 ? this.photosCatalogue() : undefined
    };

    const id = this.logementId();
    const requete = this.modeEdition() && id ? this.logementService.modifier(id, donnees) : this.logementService.creer(donnees);

    requete.subscribe({
      next: (logement) => {
        this.enCours.set(false);
        this.router.navigate(['/logements', logement.id]);
      },
      error: (err) => {
        this.enCours.set(false);
        if (err.status === 403 && err.error?.message) {
          this.erreur.set(err.error.message);
        } else {
          this.erreur.set(this.modeEdition() ? 'Erreur lors de la modification du logement' : 'Erreur lors de la création du logement');
        }
      }
    });
  }
}
