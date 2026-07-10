import { Component, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, MatIconModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly utilisateur = this.authService.utilisateur;
  readonly estConnecte = this.authService.estConnecte;
  readonly estHote = this.authService.estHote;
  readonly menuOuvert = signal(false);

  readonly emailNonVerifie = computed(() => this.estConnecte() && this.utilisateur()?.emailVerifie === false);

  readonly renvoiEnCours = signal(false);
  readonly renvoiMessage = signal<string | null>(null);

  readonly initiale = () => (this.utilisateur()?.prenom?.charAt(0) ?? '?').toUpperCase();

  basculerMenu(): void {
    this.menuOuvert.update((v) => !v);
  }

  fermerMenu(): void {
    this.menuOuvert.set(false);
  }

  renvoyerVerification(): void {
    this.renvoiEnCours.set(true);
    this.renvoiMessage.set(null);

    this.authService.renvoyerVerification().subscribe({
      next: () => {
        this.renvoiEnCours.set(false);
        this.renvoiMessage.set('Email renvoyé — vérifiez votre boîte de réception.');
      },
      error: () => {
        this.renvoiEnCours.set(false);
        this.renvoiMessage.set("Erreur lors de l'envoi.");
      }
    });
  }

  deconnexion(): void {
    this.fermerMenu();
    this.authService.deconnexion();
    this.router.navigate(['/']);
  }
}
