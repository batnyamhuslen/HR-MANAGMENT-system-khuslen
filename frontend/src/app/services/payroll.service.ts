import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PayrollRecordDto {
  id: number;
  employeeId: number;
  employeeName: string;
  payPeriodYear: number;
  payPeriodMonth: number;
  baseSalary: number;
  overtimePay: number;
  allowances: number;
  unpaidLeaveDeduction: number;
  grossSalary: number;
  socialInsuranceEmployee: number;
  incomeTax: number;
  otherDeductions: number;
  netSalary: number;
  status: string;
}

export interface PayrollRunSummary {
  processedCount: number;
  skippedCount: number;
  totalNetPayout: number;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

@Injectable({ providedIn: 'root' })
export class PayrollService {
  private apiUrl = 'http://127.0.0.1:8080/api/payroll';

  constructor(private http: HttpClient) {}

  runPayroll(year: number, month: number): Observable<PayrollRunSummary> {
    return this.http.post<PayrollRunSummary>(`${this.apiUrl}/run`, null, {
      params: { year, month }
    });
  }

  getRecords(year: number, month: number, page = 0, size = 20): Observable<PageResponse<PayrollRecordDto>> {
    return this.http.get<PageResponse<PayrollRecordDto>>(`${this.apiUrl}/records`, {
      params: { year, month, page, size }
    });
  }

  getEmployeeHistory(employeeId: number): Observable<PayrollRecordDto[]> {
    return this.http.get<PayrollRecordDto[]>(`${this.apiUrl}/records/employee/${employeeId}`);
  }

  getMyRecords(): Observable<PayrollRecordDto[]> {
    return this.http.get<PayrollRecordDto[]>(`${this.apiUrl}/my-records`);
  }

  finalizePayroll(id: number): Observable<PayrollRecordDto> {
    return this.http.post<PayrollRecordDto>(`${this.apiUrl}/${id}/finalize`, null);
  }

  getPayslip(id: number): Observable<PayrollRecordDto> {
    return this.http.get<PayrollRecordDto>(`${this.apiUrl}/${id}/payslip`);
  }
}
