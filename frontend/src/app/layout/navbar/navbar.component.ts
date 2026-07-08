import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, FormsModule, MatIconModule],
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
  readonly telephoneNonVerifie = computed(
    () => this.estConnecte() && !!this.utilisateur()?.telephone && this.utilisateur()?.telephoneVerifie === false
  );

  readonly renvoiEnCours = signal(false);
  readonly renvoiMessage = signal<string | null>(null);

  readonly codeTelephone = signal('');
  readonly verificationTelephoneEnCours = signal(false);
  readonly renvoiTelephoneEnCours = signal(false);
  readonly messageTelephone = signal<string | null>(null);

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

  verifierTelephone(): void {
    const code = this.codeTelephone().trim();
    if (!code) {
      return;
    }

    this.verificationTelephoneEnCours.set(true);
    this.messageTelephone.set(null);

    this.authService.verifierTelephone(code).subscribe({
      next: () => {
        this.verificationTelephoneEnCours.set(false);
        this.codeTelephone.set('');
        this.authService.marquerTelephoneVerifieLocalement();
      },
      error: (err) => {
        this.verificationTelephoneEnCours.set(false);
        this.messageTelephone.set(err.error?.message ?? 'Code incorrect');
      }
    });
  }

  renvoyerCodeTelephone(): void {
    this.renvoiTelephoneEnCours.set(true);
    this.messageTelephone.set(null);

    this.authService.renvoyerCodeTelephone().subscribe({
      next: () => {
        this.renvoiTelephoneEnCours.set(false);
        this.messageTelephone.set('Code renvoyé par SMS.');
      },
      error: () => {
        this.renvoiTelephoneEnCours.set(false);
        this.messageTelephone.set("Erreur lors de l'envoi.");
      }
    });
  }

  deconnexion(): void {
    this.fermerMenu();
    this.authService.deconnexion();
    this.router.navigate(['/']);
  }
}
