import { AfterViewInit, Component, ElementRef, EventEmitter, Output, ViewChild } from '@angular/core';
import { environment } from '../../../environments/environment';

declare const google: any;

const URL_SCRIPT_GOOGLE = 'https://accounts.google.com/gsi/client';

@Component({
  selector: 'app-bouton-google',
  standalone: true,
  imports: [],
  templateUrl: './bouton-google.component.html',
  styleUrl: './bouton-google.component.scss'
})
export class BoutonGoogleComponent implements AfterViewInit {
  @ViewChild('conteneur', { static: true }) conteneur!: ElementRef<HTMLDivElement>;
  @Output() identifiant = new EventEmitter<string>();
  @Output() indisponible = new EventEmitter<void>();

  ngAfterViewInit(): void {
    if (!environment.googleClientId) {
      this.indisponible.emit();
      return;
    }

    this.chargerScriptGoogle()
      .then(() => this.initialiserBouton())
      .catch(() => this.indisponible.emit());
  }

  private chargerScriptGoogle(): Promise<void> {
    return new Promise((resolve, reject) => {
      if ((window as any).google?.accounts?.id) {
        resolve();
        return;
      }

      const scriptExistant = document.querySelector(`script[src="${URL_SCRIPT_GOOGLE}"]`);
      if (scriptExistant) {
        scriptExistant.addEventListener('load', () => resolve());
        scriptExistant.addEventListener('error', () => reject());
        return;
      }

      const script = document.createElement('script');
      script.src = URL_SCRIPT_GOOGLE;
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => reject();
      document.head.appendChild(script);
    });
  }

  private initialiserBouton(): void {
    google.accounts.id.initialize({
      client_id: environment.googleClientId,
      callback: (reponse: { credential: string }) => this.identifiant.emit(reponse.credential)
    });

    google.accounts.id.renderButton(this.conteneur.nativeElement, {
      theme: 'outline',
      size: 'large',
      shape: 'pill',
      width: 320,
      text: 'continue_with'
    });
  }
}
