import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService, UserProfile } from '../../services/auth.service';
import { LeaveService, LeaveRequestDto, LeaveTypeDto, LeaveBalanceDto } from '../../services/leave.service';
import { EmployeeService, EmployeeDto } from '../../services/employee.service';
import { NotificationBellComponent } from '../../shared/components/notification-bell/notification-bell.component';

@Component({
  selector: 'app-leave-list',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, NotificationBellComponent],
  templateUrl: './leave-list.component.html',
  styleUrls: ['./leave-list.component.scss']
})
export class LeaveListComponent implements OnInit {
  sidebarOpen = signal(false);
  currentUser = signal<UserProfile | null>(null);
  requests = signal<LeaveRequestDto[]>([]);
  balances = signal<LeaveBalanceDto[]>([]);
  leaveTypes = signal<LeaveTypeDto[]>([]);
  employees = signal<EmployeeDto[]>([]);
  loading = signal(true);
  submitting = signal(false);
  errorMessage = signal('');
  successMessage = signal('');
  actionLoading = signal<number | null>(null);

  leaveForm;

  constructor(
    private router: Router,
    private fb: FormBuilder,
    private authService: AuthService,
    private leaveService: LeaveService,
    private employeeService: EmployeeService
  ) {
    this.leaveForm = this.fb.group({
      employeeId: ['', Validators.required],
      leaveTypeId: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      reason: ['']
    });
  }

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadData();
  }

  private loadCurrentUser(): void {
    this.authService.getProfile().subscribe({
      next: (user) => this.currentUser.set(user),
      error: () => {}
    });
  }

  private loadData(): void {
    this.loading.set(true);
    this.leaveService.getLeaveTypes().subscribe({
      next: (types) => this.leaveTypes.set(types),
      error: () => {}
    });
    this.employeeService.getEmployees('', undefined, undefined, 0, 200).subscribe({
      next: (page) => this.employees.set(page.content),
      error: () => {}
    });
    this.leaveService.getMyRequests().subscribe({
      next: (reqs) => {
        this.requests.set(reqs);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Чөлөөний хүсэлтүүдийг ачаалахад алдаа гарлаа');
      }
    });
    this.leaveService.getMyBalances().subscribe({
      next: (bals) => this.balances.set(bals),
      error: () => {}
    });
  }

  onSubmit(): void {
    if (this.leaveForm.invalid) return;
    this.submitting.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');
    const formValue = this.leaveForm.value;
    this.leaveService.submitRequest(
      Number(formValue.employeeId),
      Number(formValue.leaveTypeId),
      formValue.startDate ?? '',
      formValue.endDate ?? '',
      formValue.reason ?? ''
    ).subscribe({
      next: (newRequest) => {
        this.submitting.set(false);
        this.successMessage.set('Чөлөөний хүсэлт амжилттай илгээгдлээ');
        this.leaveForm.reset();
        const currentReqs = this.requests();
        currentReqs.unshift(newRequest);
        this.requests.set(currentReqs);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err.error?.message || 'Хүсэлт илгээхэд алдаа гарлаа');
      }
    });
  }

  approveRequest(id: number): void {
    this.actionLoading.set(id);
    this.leaveService.approve(id).subscribe({
      next: (updated) => {
        const arr = this.requests();
        for (let i = 0; i < arr.length; i++) {
          if (arr[i].id === id) {
            arr[i] = updated;
            break;
          }
        }
        this.requests.set(arr);
        this.actionLoading.set(null);
      },
      error: (err) => {
        this.actionLoading.set(null);
        this.errorMessage.set(err.error?.message || 'Зөвшөөрөхөд алдаа гарлаа');
      }
    });
  }

  rejectRequest(id: number): void {
    this.actionLoading.set(id);
    this.leaveService.reject(id).subscribe({
      next: (updated) => {
        const arr = this.requests();
        for (let i = 0; i < arr.length; i++) {
          if (arr[i].id === id) {
            arr[i] = updated;
            break;
          }
        }
        this.requests.set(arr);
        this.actionLoading.set(null);
      },
      error: (err) => {
        this.actionLoading.set(null);
        this.errorMessage.set(err.error?.message || 'Татгалзахад алдаа гарлаа');
      }
    });
  }

  isHrOrAdmin(): boolean {
    const role = this.authService.getRole();
    return role === 'ADMIN' || role === 'HR';
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'PENDING': return 'Хүлээгдэж буй';
      case 'APPROVED': return 'Зөвшөөрсөн';
      case 'REJECTED': return 'Татгалзсан';
      case 'CANCELLED': return 'Цуцалсан';
      default: return status;
    }
  }

  statusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'badge-pending';
      case 'APPROVED': return 'badge-approved';
      case 'REJECTED': return 'badge-rejected';
      case 'CANCELLED': return 'badge-cancelled';
      default: return '';
    }
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

  get leaveTypeName(): string {
    const id = Number(this.leaveForm.get('leaveTypeId')?.value);
    const types = this.leaveTypes();
    for (let i = 0; i < types.length; i++) {
      if (types[i].id === id) return types[i].name;
    }
    return '';
  }

  get employeeName(): string {
    const id = Number(this.leaveForm.get('employeeId')?.value);
    const emps = this.employees();
    for (let i = 0; i < emps.length; i++) {
      if (emps[i].id === id) return emps[i].lastName + ' ' + emps[i].firstName;
    }
    return '';
  }
}
