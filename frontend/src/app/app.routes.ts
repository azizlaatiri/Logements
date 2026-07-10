import { Routes } from '@angular/router';
import { authGuard, hoteGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/accueil/accueil.component').then((m) => m.AccueilComponent)
  },
  {
    path: 'connexion',
    loadComponent: () => import('./pages/connexion/connexion.component').then((m) => m.ConnexionComponent)
  },
  {
    path: 'inscription',
    loadComponent: () => import('./pages/inscription/inscription.component').then((m) => m.InscriptionComponent)
  },
  {
    path: 'verifier-email',
    loadComponent: () =>
      import('./pages/verification-email/verification-email.component').then((m) => m.VerificationEmailComponent)
  },
  {
    path: 'logements/:id',
    loadComponent: () =>
      import('./pages/logement-detail/logement-detail.component').then((m) => m.LogementDetailComponent)
  },
  {
    path: 'tableau-de-bord',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/tableau-bord/tableau-bord.component').then((m) => m.TableauBordComponent)
  },
  {
    path: 'publier',
    canActivate: [authGuard, hoteGuard],
    loadComponent: () =>
      import('./pages/publier-logement/publier-logement.component').then((m) => m.PublierLogementComponent)
  },
  {
    path: 'logements/:id/modifier',
    canActivate: [authGuard, hoteGuard],
    loadComponent: () =>
      import('./pages/publier-logement/publier-logement.component').then((m) => m.PublierLogementComponent)
  },
  {
    path: 'logements/:id/disponibilites',
    canActivate: [authGuard, hoteGuard],
    loadComponent: () =>
      import('./pages/gestion-disponibilite/gestion-disponibilite.component').then((m) => m.GestionDisponibiliteComponent)
  },
  { path: '**', redirectTo: '' }
];
