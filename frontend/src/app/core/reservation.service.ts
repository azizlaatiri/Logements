import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { NouvelleReservation, Reservation } from '../models/reservation.model';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  constructor(private http: HttpClient) {}

  reserver(logementId: number, demande: NouvelleReservation): Observable<Reservation> {
    return this.http.post<Reservation>(`${environment.apiUrl}/logements/${logementId}/reservations`, demande);
  }

  mesReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${environment.apiUrl}/reservations/moi`);
  }

  annuler(id: number): Observable<Reservation> {
    return this.http.delete<Reservation>(`${environment.apiUrl}/reservations/${id}`);
  }
}
