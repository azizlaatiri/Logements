import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { Role } from '../../models/utilisateur.model';

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
    MatProgressSpinnerModule
  ],
  templateUrl: './inscription.component.html',
  styleUrl: './inscription.component.scss'
})
export class InscriptionComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly formulaire = this.fb.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(8)]],
    role: ['VOYAGEUR' as Role, Validators.required]
  });

  readonly enCours = signal(false);
  readonly erreur = signal<string | null>(null);

  soumettre(): void {
    if (this.formulaire.invalid) {
      return;
    }

    this.enCours.set(true);
    this.erreur.set(null);

    const { nom, prenom, email, motDePasse, role } = this.formulaire.getRawValue();
    this.authService
      .inscrire({ nom: nom!, prenom: prenom!, email: email!, motDePasse: motDePasse!, role: role! })
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
  }
}
