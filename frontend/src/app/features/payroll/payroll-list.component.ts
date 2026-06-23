import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PayrollService, PayrollRecordDto, PayrollRunSummary } from '../../services/payroll.service';

@Component({
  selector: 'app-payroll-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payroll-list.component.html',
  styleUrls: ['./payroll-list.component.scss']
})
export class PayrollListComponent implements OnInit {
  year = signal(new Date().getFullYear());
  month = signal(new Date().getMonth() + 1);
  records = signal<PayrollRecordDto[]>([]);
  loading = signal(false);
  errorMessage = signal('');

  showConfirm = signal(false);
  confirmAction = signal<'run' | 'finalize' | null>(null);
  confirmRecordId = signal<number | null>(null);

  showFinalizeSuccess = signal(false);
  showRunSuccess = signal<PayrollRunSummary | null>(null);

  months = [
    { value: 1, label: '1-р сар' }, { value: 2, label: '2-р сар' },
    { value: 3, label: '3-р сар' }, { value: 4, label: '4-р сар' },
    { value: 5, label: '5-р сар' }, { value: 6, label: '6-р сар' },
    { value: 7, label: '7-р сар' }, { value: 8, label: '8-р сар' },
    { value: 9, label: '9-р сар' }, { value: 10, label: '10-р сар' },
    { value: 11, label: '11-р сар' }, { value: 12, label: '12-р сар' }
  ];

  constructor(
    private router: Router,
    private payrollService: PayrollService
  ) {}

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.payrollService.getRecords(this.year(), this.month()).subscribe({
      next: (page) => {
        this.records.set(page.content);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Цалингийн мэдээлэл ачаалахад алдаа гарлаа');
      }
    });
  }

  onRunPayroll(): void {
    this.confirmAction.set('run');
    this.confirmRecordId.set(null);
    this.showConfirm.set(true);
  }

  confirmRun(): void {
    this.showConfirm.set(false);
    this.loading.set(true);
    this.payrollService.runPayroll(this.year(), this.month()).subscribe({
      next: (summary) => {
        this.showRunSuccess.set(summary);
        this.loadRecords();
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Цалин тооцоолоход алдаа гарлаа');
      }
    });
  }

  onFinalize(id: number): void {
    this.confirmAction.set('finalize');
    this.confirmRecordId.set(id);
    this.showConfirm.set(true);
  }

  confirmFinalize(): void {
    this.showConfirm.set(false);
    const id = this.confirmRecordId();
    if (id === null) return;
    this.payrollService.finalizePayroll(id).subscribe({
      next: () => {
        this.showFinalizeSuccess.set(true);
        setTimeout(() => this.showFinalizeSuccess.set(false), 3000);
        this.loadRecords();
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Дуусгавар болгоход алдаа гарлаа');
      }
    });
  }

  cancelConfirm(): void {
    this.showConfirm.set(false);
    this.confirmAction.set(null);
    this.confirmRecordId.set(null);
  }

  viewPayslip(id: number): void {
    this.router.navigate(['/payroll/payslip', id]);
  }

  statusClass(status: string): string {
    switch (status) {
      case 'DRAFT': return 'badge-draft';
      case 'FINALIZED': return 'badge-finalized';
      case 'PAID': return 'badge-paid';
      default: return '';
    }
  }

  statusLabel(status: string): string {
    switch (status) {
      case 'DRAFT': return 'Ноорог';
      case 'FINALIZED': return 'Дуусгавар болсон';
      case 'PAID': return 'Төлсөн';
      default: return status;
    }
  }
}
