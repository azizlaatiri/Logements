import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FichierService {
  constructor(private http: HttpClient) {}

  televerser(fichier: File): Observable<{ url: string }> {
    const donnees = new FormData();
    donnees.append('fichier', fichier);
    return this.http.post<{ url: string }>(`${environment.apiUrl}/fichiers/upload`, donnees);
  }
}
