import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService, UserProfile } from '../../services/auth.service';
import { AttendanceService, AttendanceDto } from '../../services/attendance.service';
import { NotificationBellComponent } from '../../shared/components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-attendance-list',
  standalone: true,
  imports: [CommonModule, RouterModule, NotificationBellComponent],
  templateUrl: './attendance-list.component.html',
  styleUrls: ['./attendance-list.component.scss']
})
export class AttendanceListComponent implements OnInit {
  sidebarOpen = signal(false);
  currentUser = signal<UserProfile | null>(null);
  records = signal<AttendanceDto[]>([]);
  selectedDate = signal(this.todayString());
  loading = signal(true);
  errorMessage = signal('');
  actionLoading = signal<number | null>(null);

  constructor(
    private router: Router,
    private authService: AuthService,
    private attendanceService: AttendanceService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadAttendance();
  }

  private todayString(): string {
    const d = new Date();
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return y + '-' + m + '-' + day;
  }

  private loadCurrentUser(): void {
    this.authService.getProfile().subscribe({
      next: (user) => this.currentUser.set(user),
      error: () => {}
    });
  }

  private loadAttendance(): void {
    this.loading.set(true);
    this.attendanceService.getDailyAttendance(this.selectedDate()).subscribe({
      next: (data) => {
        this.records.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Ирцийн мэдээлэл ачаалахад алдаа гарлаа');
      }
    });
  }

  prevDay(): void {
    const d = new Date(this.selectedDate());
    d.setDate(d.getDate() - 1);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    this.selectedDate.set(y + '-' + m + '-' + day);
    this.loadAttendance();
  }

  nextDay(): void {
    const d = new Date(this.selectedDate());
    d.setDate(d.getDate() + 1);
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    this.selectedDate.set(y + '-' + m + '-' + day);
    this.loadAttendance();
  }

  isToday(): boolean {
    return this.selectedDate() === this.todayString();
  }

  goToday(): void {
    this.selectedDate.set(this.todayString());
    this.loadAttendance();
  }

  checkIn(employeeId: number): void {
    this.actionLoading.set(employeeId);
    this.attendanceService.checkIn(employeeId).subscribe({
      next: (record) => {
        const arr = this.records();
        let found = false;
        for (let i = 0; i < arr.length; i++) {
          if (arr[i].employeeId === employeeId) {
            found = true;
            break;
          }
        }
        if (!found) {
          const updated = this.records();
          updated.push(record);
          this.records.set(updated);
        }
        this.actionLoading.set(null);
      },
      error: (err) => {
        this.actionLoading.set(null);
        this.errorMessage.set(err.error?.message || 'Тэмдэглэхэд алдаа гарлаа');
      }
    });
  }

  checkOut(employeeId: number): void {
    this.actionLoading.set(employeeId);
    this.attendanceService.checkOut(employeeId).subscribe({
      next: (record) => {
        const arr = this.records();
        for (let i = 0; i < arr.length; i++) {
          if (arr[i].employeeId === employeeId) {
            const updated = this.records();
            updated[i] = record;
            this.records.set(updated);
            break;
          }
        }
        this.actionLoading.set(null);
      },
      error: (err) => {
        this.actionLoading.set(null);
        this.errorMessage.set(err.error?.message || 'Тэмдэглэхэд алдаа гарлаа');
      }
    });
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'PRESENT': return 'Ирсэн';
      case 'LATE': return 'Хоцорсон';
      case 'ABSENT': return 'Тасалсан';
      default: return status;
    }
  }

  statusClass(status: string): string {
    switch (status) {
      case 'PRESENT': return 'badge-present';
      case 'LATE': return 'badge-late';
      case 'ABSENT': return 'badge-absent';
      default: return '';
    }
  }

  formatTime(time: string | null): string {
    if (!time) return '-';
    return time.substring(0, 5);
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

  displayDateLabel(): string {
    const d = new Date(this.selectedDate());
    const months = ['Нэгдүгээр', 'Хоёрдугаар', 'Гуравдугаар', 'Дөрөвдүгээр', 'Тавдугаар', 'Зургадугаар', 'Долоодугаар', 'Наймдугаар', 'Есдүгээр', 'Аравдугаар', 'Арваннэгдүгээр', 'Арванхоёрдугаар'];
    return this.selectedDate().split('-')[2] + ' ' + months[d.getMonth()] + ' сар, ' + d.getFullYear();
  }
}
