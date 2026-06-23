import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { PayrollService, PayrollRecordDto } from '../../services/payroll.service';

@Component({
  selector: 'app-payslip-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payslip-detail.component.html',
  styleUrls: ['./payslip-detail.component.scss']
})
export class PayslipDetailComponent implements OnInit {
  record = signal<PayrollRecordDto | null>(null);
  loading = signal(true);
  errorMessage = signal('');

  months = ['', '1-р сар', '2-р сар', '3-р сар', '4-р сар', '5-р сар', '6-р сар',
    '7-р сар', '8-р сар', '9-р сар', '10-р сар', '11-р сар', '12-р сар'];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authService: AuthService,
    private payrollService: PayrollService,
  ) {}

  ngOnInit(): void {
    this.loadPayslip();
  }

  private loadPayslip(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage.set('Цалингийн хуудас олдсонгүй');
      this.loading.set(false);
      return;
    }
    this.payrollService.getPayslip(id).subscribe({
      next: (data) => {
        this.record.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Цалингийн хуудас ачаалахад алдаа гарлаа');
      }
    });
  }

  isManagementRole(): boolean {
    const role = this.authService.getRole();
    return role === 'ADMIN' || role === 'HR';
  }

  goBack(): void {
    if (this.isManagementRole()) {
      this.router.navigate(['/payroll']);
    } else {
      this.router.navigate(['/payroll/my-payslips']);
    }
  }
}
