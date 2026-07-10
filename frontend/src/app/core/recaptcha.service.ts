import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

declare const grecaptcha: any;

@Injectable({ providedIn: 'root' })
export class RecaptchaService {
  private chargement: Promise<void> | null = null;

  precharger(): void {
    if (environment.recaptchaSiteKey) {
      this.chargerScript();
    }
  }

  obtenirToken(action: string): Promise<string> {
    if (!environment.recaptchaSiteKey) {
      return Promise.resolve('');
    }

    const declenchement = this.chargerScript().then(
      () =>
        new Promise<string>((resolve, reject) => {
          try {
            grecaptcha.ready(() => {
              try {
                grecaptcha
                  .execute(environment.recaptchaSiteKey, { action })
                  .then((token: string) => resolve(token))
                  .catch(reject);
              } catch (erreur) {
                reject(erreur);
              }
            });
          } catch (erreur) {
            reject(erreur);
          }
        })
    );

    const delaiMax = new Promise<string>((_, reject) =>
      setTimeout(() => reject(new Error('Délai de vérification anti-bot dépassé')), 8000)
    );

    return Promise.race([declenchement, delaiMax]);
  }

  private chargerScript(): Promise<void> {
    if ((window as any).grecaptcha) {
      return Promise.resolve();
    }

    if (this.chargement) {
      return this.chargement;
    }

    this.chargement = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = `https://www.google.com/recaptcha/api.js?render=${environment.recaptchaSiteKey}`;
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => reject();
      document.head.appendChild(script);
    });

    return this.chargement;
  }
}
