import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Logement, NouveauLogement } from '../models/logement.model';

export interface CriteresRecherche {
  ville?: string;
  pays?: string;
  dateDebut?: string;
  dateFin?: string;
}

@Injectable({ providedIn: 'root' })
export class LogementService {
  constructor(private http: HttpClient) {}

  lister(): Observable<Logement[]> {
    return this.http.get<Logement[]>(`${environment.apiUrl}/logements`);
  }

  obtenir(id: number): Observable<Logement> {
    return this.http.get<Logement>(`${environment.apiUrl}/logements/${id}`);
  }

  rechercher(criteres: CriteresRecherche): Observable<Logement[]> {
    let params = new HttpParams();
    if (criteres.ville) {
      params = params.set('ville', criteres.ville);
    }
    if (criteres.pays) {
      params = params.set('pays', criteres.pays);
    }
    if (criteres.dateDebut) {
      params = params.set('dateDebut', criteres.dateDebut);
    }
    if (criteres.dateFin) {
      params = params.set('dateFin', criteres.dateFin);
    }
    return this.http.get<Logement[]>(`${environment.apiUrl}/logements/recherche`, { params });
  }

  creer(logement: NouveauLogement): Observable<Logement> {
    return this.http.post<Logement>(`${environment.apiUrl}/logements`, logement);
  }

  modifier(id: number, logement: NouveauLogement): Observable<Logement> {
    return this.http.put<Logement>(`${environment.apiUrl}/logements/${id}`, logement);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/logements/${id}`);
  }
}
