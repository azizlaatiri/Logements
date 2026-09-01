import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { NouvelleReservation, NouvelleReservationRecurrente, Reservation } from '../models/reservation.model';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  constructor(private http: HttpClient) {}

  reserver(logementId: number, demande: NouvelleReservation): Observable<Reservation> {
    return this.http.post<Reservation>(`${environment.apiUrl}/logements/${logementId}/reservations`, demande);
  }

  reserverRecurrent(logementId: number, demande: NouvelleReservationRecurrente): Observable<Reservation[]> {
    return this.http.post<Reservation[]>(
      `${environment.apiUrl}/logements/${logementId}/reservations/recurrentes`,
      demande
    );
  }

  mesReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${environment.apiUrl}/reservations/moi`);
  }

  reservationsRecues(logementId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${environment.apiUrl}/logements/${logementId}/reservations`);
  }

  annuler(id: number): Observable<Reservation> {
    return this.http.delete<Reservation>(`${environment.apiUrl}/reservations/${id}`);
  }

  confirmer(id: number): Observable<Reservation> {
    return this.http.patch<Reservation>(`${environment.apiUrl}/reservations/${id}/confirmer`, {});
  }

  payer(id: number): Observable<{ url: string }> {
    return this.http.post<{ url: string }>(`${environment.apiUrl}/reservations/${id}/paiement`, {});
  }
}
