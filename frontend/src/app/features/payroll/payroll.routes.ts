import { Routes } from '@angular/router';
import { authGuard } from '../../guards/auth.guard';

export const payrollRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./payroll-list.component').then(m => m.PayrollListComponent),
    canActivate: [authGuard],
    data: { roles: ['ADMIN', 'HR'] }
  },
  {
    path: 'my-payslips',
    loadComponent: () => import('./my-payslips.component').then(m => m.MyPayslipsComponent),
    canActivate: [authGuard],
    data: { roles: ['EMPLOYEE'] }
  },
  {
    path: 'payslip/:id',
    loadComponent: () => import('./payslip-detail.component').then(m => m.PayslipDetailComponent),
    canActivate: [authGuard]
  }
];
