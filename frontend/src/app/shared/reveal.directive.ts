import { AfterViewInit, Directive, ElementRef, Input, OnDestroy, inject } from '@angular/core';
import { AnimationBuilder, AnimationPlayer, animate, style } from '@angular/animations';

/**
 * Anime l'entree d'un element quand il devient visible au scroll, via l'API
 * Web Animations (AnimationBuilder) plutot qu'une transition CSS : ca evite
 * tout conflit de specificite avec les transitions de survol propres au
 * composant (les deux ciblant souvent `transform` sur le meme element).
 */
@Directive({
  selector: '[appReveal]',
  standalone: true
})
export class RevealDirective implements AfterViewInit, OnDestroy {
  private readonly element = inject(ElementRef<HTMLElement>);
  private readonly animationBuilder = inject(AnimationBuilder);
  private observateur?: IntersectionObserver;
  private lecteur?: AnimationPlayer;

  @Input('appReveal') delaiIndex: number | string = 0;

  ngAfterViewInit(): void {
    const hote = this.element.nativeElement;
    hote.style.opacity = '0';

    if (typeof IntersectionObserver === 'undefined') {
      hote.style.opacity = '';
      return;
    }

    this.observateur = new IntersectionObserver(
      (entrees) => {
        for (const entree of entrees) {
          if (entree.isIntersecting) {
            this.jouerAnimation();
            this.observateur?.unobserve(hote);
          }
        }
      },
      { threshold: 0.1, rootMargin: '0px 0px -60px 0px' }
    );
    this.observateur.observe(hote);
  }

  private jouerAnimation(): void {
    const hote = this.element.nativeElement;
    const delai = Math.min(Number(this.delaiIndex) || 0, 12) * 60;

    const factory = this.animationBuilder.build([
      style({ opacity: 0, transform: 'translateY(32px) scale(0.98)' }),
      animate(`700ms ${delai}ms cubic-bezier(0.22, 1, 0.36, 1)`, style({ opacity: 1, transform: 'none' }))
    ]);
    const lecteur = factory.create(hote);
    this.lecteur = lecteur;
    lecteur.onDone(() => {
      // Laisse le CSS du composant reprendre la main (survol, etc.) sans style inline residuel.
      hote.style.opacity = '';
      hote.style.transform = '';
      lecteur.destroy();
      if (this.lecteur === lecteur) {
        this.lecteur = undefined;
      }
    });
    lecteur.play();
  }

  ngOnDestroy(): void {
    this.observateur?.disconnect();
    this.lecteur?.destroy();
  }
}
