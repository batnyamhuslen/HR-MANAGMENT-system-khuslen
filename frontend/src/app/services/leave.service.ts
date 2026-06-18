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

@Injectable({ providedIn: 'root' })
export class LeaveService {
  private apiUrl = 'http://127.0.0.1:8080/api/leave-requests';

  constructor(private http: HttpClient) {}

  getPending(limit: number = 10): Observable<PendingLeaveRequest[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<PendingLeaveRequest[]>(`${this.apiUrl}/pending`, { params });
  }

  approve(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/approve`, {});
  }
}
