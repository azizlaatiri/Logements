import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((erreur) => {
      if (erreur.status === 401 && req.url.startsWith(environment.apiUrl)) {
        authService.deconnexion();
        router.navigate(['/connexion']);
      }
      return throwError(() => erreur);
    })
  );
};
