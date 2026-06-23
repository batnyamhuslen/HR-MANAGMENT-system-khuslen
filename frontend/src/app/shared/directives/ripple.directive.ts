import { Directive, ElementRef, HostListener, Renderer2, inject } from '@angular/core';

@Directive({
  selector: '[appRipple]',
  standalone: true
})
export class RippleDirective {
  private el = inject(ElementRef);
  private renderer = inject(Renderer2);

  @HostListener('mousedown', ['$event'])
  onMouseDown(event: MouseEvent): void {
    const button = this.el.nativeElement;
    if (button.disabled) return;

    const rect = button.getBoundingClientRect();
    const ripple = this.renderer.createElement('span');
    const size = Math.max(rect.width, rect.height) * 2;

    this.renderer.setStyle(ripple, 'position', 'absolute');
    this.renderer.setStyle(ripple, 'border-radius', '50%');
    this.renderer.setStyle(ripple, 'background', 'rgba(255,255,255,0.35)');
    this.renderer.setStyle(ripple, 'width', size + 'px');
    this.renderer.setStyle(ripple, 'height', size + 'px');
    this.renderer.setStyle(ripple, 'left', (event.clientX - rect.left - size / 2) + 'px');
    this.renderer.setStyle(ripple, 'top', (event.clientY - rect.top - size / 2) + 'px');
    this.renderer.setStyle(ripple, 'transform', 'scale(0)');
    this.renderer.setStyle(ripple, 'opacity', '1');
    this.renderer.setStyle(ripple, 'pointer-events', 'none');
    this.renderer.setStyle(ripple, 'transition', 'transform 300ms cubic-bezier(0.4,0,0.2,1), opacity 300ms cubic-bezier(0.4,0,0.2,1)');
    this.renderer.setStyle(button, 'position', 'relative');
    this.renderer.setStyle(button, 'overflow', 'hidden');

    this.renderer.appendChild(button, ripple);

    requestAnimationFrame(() => {
      this.renderer.setStyle(ripple, 'transform', 'scale(4)');
      this.renderer.setStyle(ripple, 'opacity', '0');
    });

    ripple.addEventListener('transitionend', () => {
      this.renderer.removeChild(button, ripple);
    }, { once: true });
  }
}
