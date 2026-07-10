import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Calendrier, Indisponibilite, NouveauBlocage, NouveauBlocageRecurrent } from '../models/disponibilite.model';

@Injectable({ providedIn: 'root' })
export class DisponibiliteService {
  constructor(private http: HttpClient) {}

  obtenirCalendrier(logementId: number): Observable<Calendrier> {
    return this.http.get<Calendrier>(`${environment.apiUrl}/logements/${logementId}/calendrier`);
  }

  lister(logementId: number): Observable<Indisponibilite[]> {
    return this.http.get<Indisponibilite[]>(`${environment.apiUrl}/logements/${logementId}/indisponibilites`);
  }

  bloquer(logementId: number, blocage: NouveauBlocage): Observable<Indisponibilite> {
    return this.http.post<Indisponibilite>(`${environment.apiUrl}/logements/${logementId}/indisponibilites`, blocage);
  }

  bloquerRecurrent(logementId: number, blocage: NouveauBlocageRecurrent): Observable<Indisponibilite[]> {
    return this.http.post<Indisponibilite[]>(
      `${environment.apiUrl}/logements/${logementId}/indisponibilites/recurrentes`,
      blocage
    );
  }

  debloquer(logementId: number, indisponibiliteId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/logements/${logementId}/indisponibilites/${indisponibiliteId}`);
  }
}
