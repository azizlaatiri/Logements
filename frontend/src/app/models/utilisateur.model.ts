export type Role = 'HOTE' | 'VOYAGEUR';

export interface Utilisateur {
  id: number;
  nom: string;
  prenom: string;
  email: string;
  role: Role;
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
}

export interface LoginRequest {
  email: string;
  motDePasse: string;
}
