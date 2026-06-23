import { Component, OnInit, signal, inject } from '@angular/core';
import { Router, RouterModule, ChildrenOutletContexts } from '@angular/router';
import { AuthService, UserProfile } from '../../../services/auth.service';
import { NotificationBellComponent } from '../notification-bell/notification-bell.component';
import { trigger, transition, query, style, animate, group, animateChild } from '@angular/animations';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterModule, NotificationBellComponent],
  templateUrl: './shell.component.html',
  styleUrls: ['./shell.component.scss'],
  animations: [
    trigger('routeAnimations', [
      transition('* <=> *', [
        style({ position: 'relative' }),
        query(':enter, :leave', [
          style({
            position: 'absolute',
            inset: 0,
            width: '100%',
          }),
        ], { optional: true }),
        query(':enter', [
          style({ opacity: 0, transform: 'translateY(6px)' }),
        ], { optional: true }),
        query(':leave', [
          style({ opacity: 1, transform: 'translateY(0)' }),
        ], { optional: true }),
        group([
          query(':leave', [
            animate('200ms cubic-bezier(0.4, 0, 1, 1)', style({ opacity: 0, transform: 'translateY(-6px)' })),
          ], { optional: true }),
          query(':enter', [
            animate('280ms 60ms cubic-bezier(0, 0, 0.2, 1)', style({ opacity: 1, transform: 'translateY(0)' })),
          ], { optional: true }),
        ]),
      ]),
    ]),
  ]
})
export class ShellComponent implements OnInit {
  sidebarOpen = signal(false);
  currentUser = signal<UserProfile | null>(null);

  private router = inject(Router);
  private authService = inject(AuthService);
  private contexts = inject(ChildrenOutletContexts);

  ngOnInit(): void {
    this.loadCurrentUser();
  }

  isHrAdmin(): boolean {
    const role = this.authService.getRole();
    return role === 'ADMIN' || role === 'HR';
  }

  private loadCurrentUser(): void {
    this.authService.getProfile().subscribe({
      next: (user) => this.currentUser.set(user),
      error: () => {}
    });
  }

  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

  getRouteAnimationData(): string {
    return this.contexts.getContext('primary')?.route?.snapshot?.data?.['animation'] || '';
  }
}
