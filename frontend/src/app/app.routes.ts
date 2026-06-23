import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.authRoutes)
  },
  {
    path: '',
    loadComponent: () => import('./shared/components/shell/shell.component').then(m => m.ShellComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
        data: { animation: 'dashboardPage' }
      },
      {
        path: 'employees',
        loadComponent: () => import('./features/employees/employee-list.component').then(m => m.EmployeeListComponent),
        data: { animation: 'employeesPage' }
      },
      {
        path: 'attendance',
        loadComponent: () => import('./features/attendance/attendance-list.component').then(m => m.AttendanceListComponent),
        data: { animation: 'attendancePage' }
      },
      {
        path: 'leave',
        loadComponent: () => import('./features/leave/leave-list.component').then(m => m.LeaveListComponent),
        data: { animation: 'leavePage' }
      },
      {
        path: 'payroll',
        loadChildren: () => import('./features/payroll/payroll.routes').then(m => m.payrollRoutes)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '/dashboard' }
];
