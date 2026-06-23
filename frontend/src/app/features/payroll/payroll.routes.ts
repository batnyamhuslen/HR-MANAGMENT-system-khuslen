import { Routes } from '@angular/router';

export const payrollRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./payroll-list.component').then(m => m.PayrollListComponent),
    data: { animation: 'payrollPage', roles: ['ADMIN', 'HR'] }
  },
  {
    path: 'my-payslips',
    loadComponent: () => import('./my-payslips.component').then(m => m.MyPayslipsComponent),
    data: { animation: 'myPayslipsPage', roles: ['EMPLOYEE'] }
  },
  {
    path: 'payslip/:id',
    loadComponent: () => import('./payslip-detail.component').then(m => m.PayslipDetailComponent),
    data: { animation: 'payslipDetailPage' }
  }
];
