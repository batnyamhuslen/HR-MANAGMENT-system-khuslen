import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService, UserProfile } from '../../services/auth.service';
import { DashboardService, DashboardStats, SalaryTrendPoint } from '../../services/dashboard.service';
import { LeaveService, PendingLeaveRequest } from '../../services/leave.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  sidebarOpen = signal(false);
  stats = signal<DashboardStats | null>(null);
  salaryTrend = signal<SalaryTrendPoint[]>([]);
  pendingLeaves = signal<PendingLeaveRequest[]>([]);
  currentUser = signal<UserProfile | null>(null);
  statsLoading = signal(true);
  chartLoading = signal(true);
  leavesLoading = signal(true);
  errorMessage = signal('');

  constructor(
    private router: Router,
    private authService: AuthService,
    private dashboardService: DashboardService,
    private leaveService: LeaveService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadStats();
    this.loadSalaryTrend();
    this.loadPendingLeaves();
  }

  private loadCurrentUser(): void {
    this.authService.getProfile().subscribe({
      next: (user) => this.currentUser.set(user),
      error: () => {}
    });
  }

  private loadStats(): void {
    this.dashboardService.getStats().subscribe({
      next: (data) => { this.stats.set(data); this.statsLoading.set(false); },
      error: () => { this.statsLoading.set(false); this.errorMessage.set('Статистик мэдээлэл ачаалахад алдаа гарлаа'); }
    });
  }

  private loadSalaryTrend(): void {
    this.dashboardService.getSalaryTrend().subscribe({
      next: (data) => { this.salaryTrend.set(data); this.chartLoading.set(false); },
      error: () => { this.chartLoading.set(false); }
    });
  }

  private loadPendingLeaves(): void {
    this.leaveService.getPending(3).subscribe({
      next: (data) => { this.pendingLeaves.set(data); this.leavesLoading.set(false); },
      error: () => { this.leavesLoading.set(false); }
    });
  }

  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }

  approveLeave(requestId: number): void {
    this.leaveService.approve(requestId).subscribe({
      next: () => {
        this.pendingLeaves.update(leaves => leaves.filter(l => l.id !== requestId));
      }
    });
  }

  barHeightPercent(point: SalaryTrendPoint): number {
    const max = Math.max(...this.salaryTrend().map(p => p.amount), 1);
    return (point.amount / max) * 100;
  }

  isLastMonth(index: number): boolean {
    return index === this.salaryTrend().length - 1;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
