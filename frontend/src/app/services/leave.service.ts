import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PendingLeaveRequest {
  id: number;
  employeeName: string;
  initials: string;
  leaveTypeName: string;
  totalDays: number;
}

export interface LeaveRequestDto {
  id: number;
  employeeId: number;
  employeeName: string;
  leaveTypeId: number;
  leaveTypeName: string;
  startDate: string;
  endDate: string;
  totalDays: number;
  reason: string;
  status: string;
  approvedById: number;
  approvedByName: string;
  approvedAt: string;
  createdAt: string;
}

export interface LeaveBalanceDto {
  id: number;
  employeeId: number;
  leaveTypeId: number;
  leaveTypeName: string;
  year: number;
  totalAllocated: number;
  used: number;
  remaining: number;
}

export interface LeaveTypeDto {
  id: number;
  name: string;
  defaultDaysPerYear: number;
}

@Injectable({ providedIn: 'root' })
export class LeaveService {
  private apiUrl = 'http://127.0.0.1:8080/api/leave-requests';
  private leaveTypeUrl = 'http://127.0.0.1:8080/api/leave-types';

  constructor(private http: HttpClient) {}

  getPending(limit: number = 10): Observable<PendingLeaveRequest[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<PendingLeaveRequest[]>(`${this.apiUrl}/pending`, { params });
  }

  approve(id: number): Observable<LeaveRequestDto> {
    return this.http.post<LeaveRequestDto>(`${this.apiUrl}/${id}/approve`, {});
  }

  reject(id: number): Observable<LeaveRequestDto> {
    return this.http.post<LeaveRequestDto>(`${this.apiUrl}/${id}/reject`, {});
  }

  submitRequest(
    employeeId: number,
    leaveTypeId: number,
    startDate: string,
    endDate: string,
    reason?: string
  ): Observable<LeaveRequestDto> {
    let params = new HttpParams()
      .set('employeeId', employeeId.toString())
      .set('leaveTypeId', leaveTypeId.toString())
      .set('startDate', startDate)
      .set('endDate', endDate);
    if (reason) params = params.set('reason', reason);
    return this.http.post<LeaveRequestDto>(this.apiUrl, null, { params });
  }

  getMyRequests(): Observable<LeaveRequestDto[]> {
    return this.http.get<LeaveRequestDto[]>(`${this.apiUrl}/my`);
  }

  getMyBalances(): Observable<LeaveBalanceDto[]> {
    return this.http.get<LeaveBalanceDto[]>(`${this.apiUrl}/balances`);
  }

  getLeaveTypes(): Observable<LeaveTypeDto[]> {
    return this.http.get<LeaveTypeDto[]>(this.leaveTypeUrl);
  }
}
