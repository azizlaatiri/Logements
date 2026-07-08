import { Component, OnInit, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-verification-email',
  standalone: true,
  imports: [RouterLink, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './verification-email.component.html',
  styleUrl: './verification-email.component.scss'
})
export class VerificationEmailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);

  readonly statut = signal<'chargement' | 'succes' | 'erreur'>('chargement');
  readonly messageErreur = signal<string | null>(null);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.statut.set('erreur');
      this.messageErreur.set('Lien de vérification invalide.');
      return;
    }

    this.authService.verifierEmail(token).subscribe({
      next: ({ email }) => {
        this.authService.marquerEmailVerifieLocalement(email);
        this.statut.set('succes');
      },
      error: (err) => {
        this.statut.set('erreur');
        this.messageErreur.set(err.error?.message ?? 'Erreur lors de la vérification de votre email.');
      }
    });
  }
}
