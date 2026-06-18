import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService, UserProfile } from '../../services/auth.service';
import { EmployeeService, EmployeeDto } from '../../services/employee.service';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './employee-list.component.html',
  styleUrls: ['./employee-list.component.scss']
})
export class EmployeeListComponent implements OnInit {
  sidebarOpen = signal(false);
  currentUser = signal<UserProfile | null>(null);
  allEmployees = signal<EmployeeDto[]>([]);
  searchQuery = signal('');
  loading = signal(true);
  errorMessage = signal('');
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);

  constructor(
    private authService: AuthService,
    private employeeService: EmployeeService
  ) {}

  ngOnInit(): void {
    this.loadCurrentUser();
    this.loadEmployees();
  }

  private loadCurrentUser(): void {
    this.authService.getProfile().subscribe({
      next: (user) => this.currentUser.set(user),
      error: () => {}
    });
  }

  private loadEmployees(): void {
    this.loading.set(true);
    this.employeeService.getEmployees('', undefined, undefined, this.currentPage(), 100).subscribe({
      next: (page) => {
        this.allEmployees.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Ажилтнуудын мэдээллийг ачаалахад алдаа гарлаа');
      }
    });
  }

  filteredEmployees(): EmployeeDto[] {
    const q = this.searchQuery().toLowerCase();
    if (q === '') {
      return this.allEmployees();
    }
    const arr = this.allEmployees();
    const result: EmployeeDto[] = [];
    for (let i = 0; i < arr.length; i++) {
      const e = arr[i];
      const fullName = (e.lastName + ' ' + e.firstName).toLowerCase();
      if (fullName.indexOf(q) !== -1) {
        result.push(e);
      }
    }
    return result;
  }

  prevPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.loadEmployees();
    }
  }

  nextPage(): void {
    if (this.currentPage() < this.totalPages() - 1) {
      this.currentPage.update(p => p + 1);
      this.loadEmployees();
    }
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'Идэвхтэй';
      case 'ON_LEAVE': return 'Чөлөөтэй';
      case 'TERMINATED': return 'Чөлөөлсөн';
      default: return status;
    }
  }

  statusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'badge-active';
      case 'ON_LEAVE': return 'badge-onleave';
      case 'TERMINATED': return 'badge-terminated';
      default: return '';
    }
  }

  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
  }

  closeSidebar(): void {
    this.sidebarOpen.set(false);
  }
}
