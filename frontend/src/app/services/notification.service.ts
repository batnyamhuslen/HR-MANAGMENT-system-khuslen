import { Injectable, Inject, PLATFORM_ID, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { interval, switchMap } from 'rxjs';

export interface AppNotification {
  id: number;
  type: string;
  title: string;
  message: string;
  linkPath: string | null;
  relatedEntityId: number | null;
  isRead: boolean;
  createdAt: string;
}

export interface NotificationPage {
  content: AppNotification[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private http = inject(HttpClient);
  private apiUrl = 'http://127.0.0.1:8080/api/notifications';

  unreadCount = signal(0);
  notifications = signal<AppNotification[]>([]);

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  startPolling(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.loadUnreadCount();
    interval(30000).pipe(
      switchMap(() => this.http.get<{ count: number }>(`${this.apiUrl}/unread-count`))
    ).subscribe({
      next: (res) => this.unreadCount.set(res.count),
      error: () => {}
    });
  }

  loadUnreadCount(): void {
    this.http.get<{ count: number }>(`${this.apiUrl}/unread-count`).subscribe({
      next: (res) => this.unreadCount.set(res.count),
      error: () => {}
    });
  }

  loadNotifications(): void {
    this.http.get<NotificationPage>(`${this.apiUrl}?page=0&size=10`).subscribe({
      next: (page) => this.notifications.set(page.content),
      error: () => {}
    });
  }

  markAsRead(id: number): void {
    this.http.post(`${this.apiUrl}/${id}/read`, {}).subscribe({
      next: () => {
        this.notifications.update(list =>
          list.map(n => n.id === id ? { ...n, isRead: true } : n)
        );
        this.unreadCount.update(c => Math.max(0, c - 1));
      },
      error: () => {}
    });
  }

  markAllAsRead(): void {
    this.http.post(`${this.apiUrl}/read-all`, {}).subscribe({
      next: () => {
        this.notifications.update(list =>
          list.map(n => ({ ...n, isRead: true }))
        );
        this.unreadCount.set(0);
      },
      error: () => {}
    });
  }
}
