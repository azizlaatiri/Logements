import { Component, OnInit, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { RecaptchaService } from '../../core/recaptcha.service';
import { Role } from '../../models/utilisateur.model';
import { BoutonGoogleComponent } from '../../shared/bouton-google/bouton-google.component';

function motsDePasseIdentiquesValidator(groupe: AbstractControl): ValidationErrors | null {
  const motDePasse = groupe.get('motDePasse')?.value;
  const confirmation = groupe.get('confirmationMotDePasse')?.value;
  return motDePasse === confirmation ? null : { motsDePasseDifferents: true };
}

@Component({
  selector: 'app-inscription',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatProgressSpinnerModule,
    BoutonGoogleComponent
  ],
  templateUrl: './inscription.component.html',
  styleUrl: './inscription.component.scss'
})
export class InscriptionComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly recaptchaService = inject(RecaptchaService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.recaptchaService.precharger();
  }

  readonly formulaire = this.fb.group(
    {
      nom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      prenom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      telephone: ['', [Validators.required, Validators.pattern(/^\+?[0-9 .-]{6,20}$/)]],
      motDePasse: ['', [Validators.required, Validators.minLength(8)]],
      confirmationMotDePasse: ['', Validators.required],
      role: ['VOYAGEUR' as Role, Validators.required]
    },
    { validators: motsDePasseIdentiquesValidator }
  );

  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);
  readonly googleDisponible = signal(true);

  soumettre(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    this.enCours.set(true);
    this.erreur.set(null);

    const { nom, prenom, email, telephone, motDePasse, role } = this.formulaire.getRawValue();

    this.recaptchaService
      .obtenirToken('inscription')
      .then((captchaToken) => {
        this.authService
          .inscrire(
            { nom: nom!, prenom: prenom!, email: email!, telephone: telephone!, motDePasse: motDePasse!, role: role! },
            captchaToken
          )
          .subscribe({
            next: () => {
              this.enCours.set(false);
              this.router.navigate(['/']);
            },
            error: (err) => {
              this.enCours.set(false);
              this.erreur.set(err.status === 409 ? 'Un compte existe déjà avec cet email' : "Erreur lors de l'inscription");
            }
          });
      })
      .catch(() => {
        this.enCours.set(false);
        this.erreur.set('Erreur de vérification anti-bot, veuillez réessayer.');
      });
  }

  connexionGoogle(idToken: string): void {
    this.enCours.set(true);
    this.erreur.set(null);

    this.authService.connexionGoogle(idToken).subscribe({
      next: () => {
        this.enCours.set(false);
        this.router.navigate(['/']);
      },
      error: () => {
        this.enCours.set(false);
        this.erreur.set('Erreur lors de la connexion avec Google');
      }
    });
  }
}
