import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService, UserProfile } from '../../services/auth.service';
import { DashboardService, DashboardStats, EmployeeDashboardStats, SalaryTrendPoint } from '../../services/dashboard.service';
import { LeaveService, PendingLeaveRequest } from '../../services/leave.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  stats = signal<DashboardStats | null>(null);
  myStats = signal<EmployeeDashboardStats | null>(null);
  salaryTrend = signal<SalaryTrendPoint[]>([]);
  pendingLeaves = signal<PendingLeaveRequest[]>([]);
  currentUser = signal<UserProfile | null>(null);
  statsLoading = signal(true);
  chartLoading = signal(true);
  leavesLoading = signal(true);
  errorMessage = signal('');
  isManagementRole = signal(false);
  canViewAttendanceStats = computed(() => {
    const role = this.currentUser()?.role;
    return role === 'ADMIN' || role === 'HR';
  });

  constructor(
    private authService: AuthService,
    private dashboardService: DashboardService,
    private leaveService: LeaveService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadSalaryTrend();
    this.loadPendingLeaves();
  }

  private loadCurrentUser(): void {
    this.authService.getProfile().subscribe({
      next: (user) => {
        this.currentUser.set(user);
        const managementRoles = ['ADMIN', 'HR', 'MANAGER'];
        let isMgmt = false;
        for (let i = 0; i < managementRoles.length; i++) {
          if (user.role === managementRoles[i]) {
            isMgmt = true;
            break;
          }
        }
        this.isManagementRole.set(isMgmt);
        this.loadStats();
      },
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

  approveLeave(requestId: number): void {
    this.leaveService.approve(requestId).subscribe({
      next: () => {
        const current = this.pendingLeaves();
        const updated = [];
        for (let i = 0; i < current.length; i++) {
          if (current[i].id !== requestId) {
            updated.push(current[i]);
          }
        }
        this.pendingLeaves.set(updated);
      }
    });
  }

  barHeightPercent(point: SalaryTrendPoint): number {
    const trend = this.salaryTrend();
    let max = 1;
    for (let i = 0; i < trend.length; i++) {
      if (trend[i].amount > max) {
        max = trend[i].amount;
      }
    }
    return (point.amount / max) * 100;
  }

  isLastMonth(index: number): boolean {
    return index === this.salaryTrend().length - 1;
  }
}
