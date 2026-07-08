import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { BoutonGoogleComponent } from '../../shared/bouton-google/bouton-google.component';

@Component({
  selector: 'app-connexion',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    BoutonGoogleComponent
  ],
  templateUrl: './connexion.component.html',
  styleUrl: './connexion.component.scss'
})
export class ConnexionComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly formulaire = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', Validators.required]
  });

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

    const { email, motDePasse } = this.formulaire.getRawValue();
    this.authService.login({ email: email!, motDePasse: motDePasse! }).subscribe({
      next: () => {
        this.enCours.set(false);
        this.router.navigate(['/']);
      },
      error: () => {
        this.enCours.set(false);
        this.erreur.set('Email ou mot de passe incorrect');
      }
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
