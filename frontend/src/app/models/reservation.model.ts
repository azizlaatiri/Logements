import { FrequenceRecurrence } from './disponibilite.model';
import { Logement } from './logement.model';
import { Utilisateur } from './utilisateur.model';

export type StatutReservation = 'EN_ATTENTE' | 'CONFIRMEE' | 'ANNULEE';

export interface Reservation {
  id: number;
  logement: Logement;
  voyageur: Utilisateur;
  dateDebut: string;
  dateFin: string;
  prixTotal: number;
  statut: StatutReservation;
  dateCreation: string;
  groupeRecurrenceId?: string;
}

export interface NouvelleReservation {
  dateDebut: string;
  dateFin: string;
}

export interface NouvelleReservationRecurrente {
  dateDebut: string;
  dateFin: string;
  frequence: FrequenceRecurrence;
  nombreOccurrences: number;
}
