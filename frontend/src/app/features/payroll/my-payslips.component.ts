import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { PayrollService, PayrollRecordDto } from '../../services/payroll.service';

@Component({
  selector: 'app-my-payslips',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-payslips.component.html',
  styleUrls: ['./my-payslips.component.scss']
})
export class MyPayslipsComponent implements OnInit {
  records = signal<PayrollRecordDto[]>([]);
  loading = signal(true);
  errorMessage = signal('');

  months = ['', '1-р сар', '2-р сар', '3-р сар', '4-р сар', '5-р сар', '6-р сар',
    '7-р сар', '8-р сар', '9-р сар', '10-р сар', '11-р сар', '12-р сар'];

  constructor(
    private router: Router,
    private payrollService: PayrollService,
  ) {}

  ngOnInit(): void {
    this.loadMyRecords();
  }

  private loadMyRecords(): void {
    this.payrollService.getMyRecords().subscribe({
      next: (data) => {
        this.records.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Цалингийн мэдээлэл ачаалахад алдаа гарлаа');
      }
    });
  }

  viewPayslip(id: number): void {
    this.router.navigate(['/payroll/payslip', id]);
  }
}
