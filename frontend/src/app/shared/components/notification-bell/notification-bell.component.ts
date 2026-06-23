import { Component, signal, inject } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService, AppNotification } from '../../../services/notification.service';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  template: `
    <div class="notif-wrapper">
      <button class="notif-btn" (click)="toggleDropdown()">
        <i class="ti ti-bell"></i>
        @if (unreadCount() > 0) {
          <span class="notif-dot" [class.pulsing]="unreadCount() > 0"></span>
        }
      </button>
      @if (isOpen()) {
        <div class="notif-dropdown anim-dropdown-open">
          <div class="notif-header">
            <span class="notif-header-title">Мэдэгдэл</span>
            @if (unreadCount() > 0) {
              <button class="mark-all-btn" (click)="markAllRead()">Бүгдийг уншсан болгох</button>
            }
          </div>
          <div class="notif-list">
            @for (notif of notifications(); track notif.id; let i = $index) {
              <div class="notif-item notif-stagger" [class.unread]="!notif.isRead" [style.--stagger-index]="i" (click)="handleClick(notif)">
                <div class="notif-title">{{ notif.title }}</div>
                <div class="notif-message">{{ notif.message }}</div>
                <div class="notif-time">{{ relativeTime(notif.createdAt) }}</div>
              </div>
            } @empty {
              <div class="notif-empty">Мэдэгдэл байхгүй</div>
            }
          </div>
        </div>
      }
    </div>
  `,
  styleUrls: ['./notification-bell.component.scss']
})
export class NotificationBellComponent {
  private notificationService = inject(NotificationService);
  private router = inject(Router);
  isOpen = signal(false);

  get unreadCount() {
    return this.notificationService.unreadCount;
  }

  get notifications() {
    return this.notificationService.notifications;
  }

  toggleDropdown(): void {
    const next = !this.isOpen();
    this.isOpen.set(next);
    if (next) {
      this.notificationService.loadNotifications();
    }
  }

  markAllRead(): void {
    this.notificationService.markAllAsRead();
  }

  handleClick(notif: AppNotification): void {
    if (!notif.isRead) {
      this.notificationService.markAsRead(notif.id);
    }
    if (notif.linkPath) {
      this.router.navigateByUrl(notif.linkPath);
    }
    this.isOpen.set(false);
  }

  relativeTime(dateStr: string): string {
    const now = new Date();
    const date = new Date(dateStr);
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    if (diffMins < 1) return 'дөнгөж сая';
    if (diffMins < 60) return diffMins + ' минутын өмнө';
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return diffHours + ' цагийн өмнө';
    const diffDays = Math.floor(diffHours / 24);
    if (diffDays < 7) return diffDays + ' өдрийн өмнө';
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return this.pad(month) + '-' + this.pad(day);
  }

  private pad(n: number): string {
    return n < 10 ? '0' + n : String(n);
  }
}
