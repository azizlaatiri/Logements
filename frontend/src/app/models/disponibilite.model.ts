export type FrequenceRecurrence = 'HEBDOMADAIRE' | 'MENSUELLE';

export interface Indisponibilite {
  id: number;
  dateDebut: string;
  dateFin: string;
  motif?: string;
  groupeRecurrenceId?: string;
}

export interface NouveauBlocage {
  dateDebut: string;
  dateFin: string;
  motif?: string;
}

export interface NouveauBlocageRecurrent {
  dateDebut: string;
  dateFin: string;
  frequence: FrequenceRecurrence;
  nombreOccurrences: number;
  motif?: string;
}

export interface PeriodeCalendrier {
  dateDebut: string;
  dateFin: string;
}

export interface Calendrier {
  periodesReservees: PeriodeCalendrier[];
  periodesBloquees: PeriodeCalendrier[];
}
