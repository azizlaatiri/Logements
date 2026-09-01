import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Avis, NouvelAvis } from '../models/avis.model';

@Injectable({ providedIn: 'root' })
export class AvisService {
  constructor(private http: HttpClient) {}

  lister(logementId: number): Observable<Avis[]> {
    return this.http.get<Avis[]>(`${environment.apiUrl}/logements/${logementId}/avis`);
  }

  creer(logementId: number, avis: NouvelAvis): Observable<Avis> {
    return this.http.post<Avis>(`${environment.apiUrl}/logements/${logementId}/avis`, avis);
  }
}
