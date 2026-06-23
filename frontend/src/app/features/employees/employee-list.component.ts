import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { EmployeeService, EmployeeDto } from '../../services/employee.service';
import { DepartmentService, DepartmentDto } from '../../services/department.service';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
  templateUrl: './employee-list.component.html',
  styleUrls: ['./employee-list.component.scss']
})
export class EmployeeListComponent implements OnInit {
  allEmployees = signal<EmployeeDto[]>([]);
  departments = signal<DepartmentDto[]>([]);
  searchQuery = signal('');
  loading = signal(true);
  errorMessage = signal('');
  currentPage = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);

  showModal = signal(false);
  modalLoading = signal(false);
  modalError = signal('');
  employeeForm;

  deleteConfirmId = signal<number | null>(null);
  removingId = signal<number | null>(null);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private employeeService: EmployeeService,
    private departmentService: DepartmentService
  ) {
    this.employeeForm = this.fb.group({
      employeeCode: ['', Validators.required],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phone: [''],
      position: [''],
      departmentId: [''],
      salary: [''],
      hireDate: ['', Validators.required],
      dateOfBirth: [''],
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.loadDepartments();
    this.loadEmployees();
  }

  private loadDepartments(): void {
    this.departmentService.getAll().subscribe({
      next: (data) => this.departments.set(data),
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

  isHrOrAdmin(): boolean {
    const role = this.authService.getRole();
    const allowed = ['ADMIN', 'HR'];
    for (let i = 0; i < allowed.length; i++) {
      if (role === allowed[i]) return true;
    }
    return false;
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

  trackById(index: number, item: any): number {
    return item.id;
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

  openAddModal(): void {
    this.employeeForm.reset();
    this.modalError.set('');
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
    this.modalError.set('');
  }

  onSubmitEmployee(): void {
    if (this.employeeForm.invalid) return;
    this.modalLoading.set(true);
    this.modalError.set('');

    const formValue = this.employeeForm.value;
    const payload: any = {
      employeeCode: formValue.employeeCode,
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      email: formValue.email,
      phone: formValue.phone || null,
      position: formValue.position || null,
      departmentId: formValue.departmentId ? Number(formValue.departmentId) : null,
      salary: formValue.salary ? Number(formValue.salary) : null,
      hireDate: formValue.hireDate,
      dateOfBirth: formValue.dateOfBirth || null,
      username: formValue.username,
      password: formValue.password
    };

    this.employeeService.createWithUser(payload).subscribe({
      next: () => {
        this.modalLoading.set(false);
        this.closeModal();
        this.loadEmployees();
      },
      error: (err) => {
        this.modalLoading.set(false);
        this.modalError.set(err.error?.message || 'Ажилтан нэмэхэд алдаа гарлаа');
      }
    });
  }

  confirmDelete(id: number): void {
    this.deleteConfirmId.set(id);
  }

  cancelDelete(): void {
    this.deleteConfirmId.set(null);
  }

  executeDelete(id: number): void {
    this.removingId.set(id);
    setTimeout(() => {
      this.employeeService.deleteEmployee(id).subscribe({
        next: () => {
          this.deleteConfirmId.set(null);
          this.removingId.set(null);
          this.loadEmployees();
        },
        error: (err) => {
          this.deleteConfirmId.set(null);
          this.removingId.set(null);
          this.errorMessage.set(err.error?.message || 'Устгахад алдаа гарлаа');
        }
      });
    }, 200);
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
}
