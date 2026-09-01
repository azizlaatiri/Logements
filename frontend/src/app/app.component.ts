import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { animate, query, style, transition, trigger } from '@angular/animations';
import { NavbarComponent } from './layout/navbar/navbar.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  animations: [
    trigger('animationRoute', [
      transition('* => *', [
        query(
          ':enter',
          [
            style({ opacity: 0, transform: 'translateY(28px) scale(0.98)' }),
            animate('420ms cubic-bezier(0.22, 1, 0.36, 1)', style({ opacity: 1, transform: 'translateY(0) scale(1)' }))
          ],
          { optional: true }
        )
      ])
    ])
  ]
})
export class AppComponent {
  preparerRoute(outlet: RouterOutlet): string {
    if (!outlet?.isActivated) {
      return '';
    }
    return outlet.activatedRouteData?.['animation'] ?? outlet.activatedRoute?.snapshot?.url?.join('/') ?? '';
  }
}
