import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.authRoutes)
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'employees',
    loadComponent: () => import('./features/employees/employee-list.component').then(m => m.EmployeeListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'attendance',
    loadComponent: () => import('./features/attendance/attendance-list.component').then(m => m.AttendanceListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'leave',
    loadComponent: () => import('./features/leave/leave-list.component').then(m => m.LeaveListComponent),
    canActivate: [authGuard]
  },
  {
    path: 'payroll',
    loadChildren: () => import('./features/payroll/payroll.routes').then(m => m.payrollRoutes)
  },
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: '**', redirectTo: '/dashboard' }
];
