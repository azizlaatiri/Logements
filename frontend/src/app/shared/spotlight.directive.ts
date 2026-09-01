import { Directive, ElementRef, HostListener, inject } from '@angular/core';

@Directive({
  selector: '[appSpotlight]',
  standalone: true
})
export class SpotlightDirective {
  private readonly element = inject(ElementRef<HTMLElement>);

  constructor() {
    this.element.nativeElement.classList.add('spotlight');
  }

  @HostListener('mousemove', ['$event'])
  surDeplacement(evenement: MouseEvent): void {
    const rect = this.element.nativeElement.getBoundingClientRect();
    const x = ((evenement.clientX - rect.left) / rect.width) * 100;
    const y = ((evenement.clientY - rect.top) / rect.height) * 100;
    this.element.nativeElement.style.setProperty('--spot-x', `${x}%`);
    this.element.nativeElement.style.setProperty('--spot-y', `${y}%`);
  }
}
