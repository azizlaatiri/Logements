import { HttpClient, HttpHeaders } from '@angular/common/http';
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

  inscrire(requete: InscriptionRequest, captchaToken: string): Observable<AuthResponse> {
    const headers = captchaToken ? new HttpHeaders({ 'X-Captcha-Token': captchaToken }) : undefined;
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/inscription`, requete, { headers })
      .pipe(tap((reponse) => this.enregistrerSession(reponse)));
  }

  connexionGoogle(idToken: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/google`, { idToken })
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

  verifierEmail(token: string): Observable<{ message: string; email: string }> {
    return this.http.get<{ message: string; email: string }>(`${environment.apiUrl}/auth/verifier-email`, {
      params: { token }
    });
  }

  renvoyerVerification(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiUrl}/auth/renvoyer-verification`, {});
  }

  marquerEmailVerifieLocalement(email: string): void {
    const utilisateur = this.utilisateurSignal();
    if (!utilisateur || utilisateur.email !== email) {
      return;
    }
    const misAJour = { ...utilisateur, emailVerifie: true };
    localStorage.setItem(CLE_UTILISATEUR, JSON.stringify(misAJour));
    this.utilisateurSignal.set(misAJour);
  }

  verifierTelephone(code: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiUrl}/auth/verifier-telephone`, { code });
  }

  renvoyerCodeTelephone(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${environment.apiUrl}/auth/renvoyer-code-telephone`, {});
  }

  marquerTelephoneVerifieLocalement(): void {
    const utilisateur = this.utilisateurSignal();
    if (!utilisateur) {
      return;
    }
    const misAJour = { ...utilisateur, telephoneVerifie: true };
    localStorage.setItem(CLE_UTILISATEUR, JSON.stringify(misAJour));
    this.utilisateurSignal.set(misAJour);
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
