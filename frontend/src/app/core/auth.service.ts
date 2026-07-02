import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, InscriptionRequest, LoginRequest, Utilisateur } from '../models/utilisateur.model';

const CLE_TOKEN = 'logements_token';
const CLE_UTILISATEUR = 'logements_utilisateur';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly utilisateurSignal = signal<Utilisateur | null>(this.lireUtilisateurStocke());

  readonly utilisateur = this.utilisateurSignal.asReadonly();
  readonly estConnecte = computed(() => this.utilisateurSignal() !== null);
  readonly estHote = computed(() => this.utilisateurSignal()?.role === 'HOTE');

  constructor(private http: HttpClient) {}

  login(requete: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, requete)
      .pipe(tap((reponse) => this.enregistrerSession(reponse)));
  }

  inscrire(requete: InscriptionRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/inscription`, requete)
      .pipe(tap((reponse) => this.enregistrerSession(reponse)));
  }

  deconnexion(): void {
    localStorage.removeItem(CLE_TOKEN);
    localStorage.removeItem(CLE_UTILISATEUR);
    this.utilisateurSignal.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(CLE_TOKEN);
  }

  private enregistrerSession(reponse: AuthResponse): void {
    localStorage.setItem(CLE_TOKEN, reponse.token);
    localStorage.setItem(CLE_UTILISATEUR, JSON.stringify(reponse.utilisateur));
    this.utilisateurSignal.set(reponse.utilisateur);
  }

  private lireUtilisateurStocke(): Utilisateur | null {
    const brut = localStorage.getItem(CLE_UTILISATEUR);
    return brut ? (JSON.parse(brut) as Utilisateur) : null;
  }
}
