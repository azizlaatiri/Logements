export type Role = 'HOTE' | 'VOYAGEUR';

export interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
  emailVerifie: boolean;
  telephone?: string;
  telephoneVerifie: boolean;
}

export interface AuthResponse {
  token: string;
  utilisateur: Utilisateur;
}

export interface InscriptionRequest {
  nom: string;
  prenom: string;
  email: string;
  motDePasse: string;
  role: Role;
  telephone?: string;
}

export interface LoginRequest {
  email: string;
  motDePasse: string;
}
