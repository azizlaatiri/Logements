import { Utilisateur } from './utilisateur.model';

export interface Logement {
  id: number;
  titre: string;
  description?: string;
  ville: string;
  adresse?: string;
  pays?: string;
  prixParNuit: number;
  nombreChambres?: number;
  nombreVoyageursMax?: number;
  imageUrl?: string;
  photos?: string[];
  proprietaire?: Utilisateur;
  noteMoyenne?: number | null;
  nombreAvis?: number;
}

export interface NouveauLogement {
  titre: string;
  description?: string;
  ville: string;
  adresse?: string;
  pays?: string;
  prixParNuit: number;
  nombreChambres?: number;
  nombreVoyageursMax?: number;
  imageUrl?: string;
  photos?: string[];
}
