import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AttendanceDto {
  id: number;
  employeeId: number;
  employeeName: string;
  date: string;
  checkInTime: string;
  checkOutTime: string;
  status: string;
  totalHours: number;
  notes: string;
}

@Injectable({ providedIn: 'root' })
export class AttendanceService {
  private apiUrl = 'http://127.0.0.1:8080/api/attendance';

  constructor(private http: HttpClient) {}

  getDailyAttendance(date?: string): Observable<AttendanceDto[]> {
    let params = new HttpParams();
    if (date) params = params.set('date', date);
    return this.http.get<AttendanceDto[]>(`${this.apiUrl}/daily`, { params });
  }

  checkIn(employeeId: number): Observable<AttendanceDto> {
    const params = new HttpParams().set('employeeId', employeeId.toString());
    return this.http.post<AttendanceDto>(`${this.apiUrl}/check-in`, null, { params });
  }

  checkOut(employeeId: number): Observable<AttendanceDto> {
    const params = new HttpParams().set('employeeId', employeeId.toString());
    return this.http.post<AttendanceDto>(`${this.apiUrl}/check-out`, null, { params });
  }
}
