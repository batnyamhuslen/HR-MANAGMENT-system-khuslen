import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DashboardStats {
  totalEmployees: number;
  attendanceRatePercent: number;
  employeesOnLeaveToday: number;
}

export interface SalaryTrendPoint {
  month: string;
  amount: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private apiUrl = 'http://127.0.0.1:8080/api/dashboard';

  constructor(private http: HttpClient) {}

  getStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
  }

  getSalaryTrend(): Observable<SalaryTrendPoint[]> {
    return this.http.get<SalaryTrendPoint[]>(`${this.apiUrl}/salary-trend`);
  }
}
