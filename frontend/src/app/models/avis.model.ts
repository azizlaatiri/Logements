import { Utilisateur } from './utilisateur.model';

export interface Avis {
  id: number;
  voyageur: Utilisateur;
  note: number;
  commentaire?: string;
  dateCreation: string;
}

export interface NouvelAvis {
  note: number;
  commentaire?: string;
}
