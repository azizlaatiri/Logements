import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

export interface SuggestionLieu {
  libelle: string;
  ville: string;
  pays: string;
}

interface ResultatNominatim {
  display_name: string;
  address?: {
    city?: string;
    town?: string;
    village?: string;
    municipality?: string;
    country?: string;
  };
}

const URL_NOMINATIM = 'https://nominatim.openstreetmap.org/search';

@Injectable({ providedIn: 'root' })
export class GeocodageService {
  constructor(private http: HttpClient) {}

  rechercherVilles(recherche: string): Observable<SuggestionLieu[]> {
    if (!recherche || recherche.trim().length < 2) {
      return of([]);
    }

    const params = new HttpParams()
      .set('format', 'jsonv2')
      .set('addressdetails', '1')
      .set('featureType', 'settlement')
      .set('accept-language', 'fr')
      .set('limit', '6')
      .set('q', recherche);

    return this.http.get<ResultatNominatim[]>(URL_NOMINATIM, { params }).pipe(
      map((resultats) => resultats.map((r) => this.versSuggestion(r)).filter((s) => s.ville))
    );
  }

  rechercherAdresses(recherche: string): Observable<SuggestionLieu[]> {
    if (!recherche || recherche.trim().length < 3) {
      return of([]);
    }

    const params = new HttpParams()
      .set('format', 'jsonv2')
      .set('addressdetails', '1')
      .set('accept-language', 'fr')
      .set('limit', '6')
      .set('q', recherche);

    return this.http
      .get<ResultatNominatim[]>(URL_NOMINATIM, { params })
      .pipe(map((resultats) => resultats.map((r) => this.versSuggestion(r))));
  }

  private versSuggestion(resultat: ResultatNominatim): SuggestionLieu {
    const adresse = resultat.address;
    return {
      libelle: resultat.display_name,
      ville: adresse?.city ?? adresse?.town ?? adresse?.village ?? adresse?.municipality ?? '',
      pays: adresse?.country ?? ''
    };
  }
}
