import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EmployeeDto {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  dateOfBirth: string;
  hireDate: string;
  departmentId: number;
  departmentName: string;
  position: string;
  salary: number;
  status: string;
  managerId: number;
  managerName: string;
  userId: number;
}

export interface EmployeePage {
  content: EmployeeDto[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private apiUrl = 'http://127.0.0.1:8080/api/employees';

  constructor(private http: HttpClient) {}

  getEmployees(
    search?: string,
    departmentId?: number,
    status?: string,
    page: number = 0,
    size: number = 100,
    sortBy: string = 'lastName',
    sortDir: string = 'asc'
  ): Observable<EmployeePage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    if (search) params = params.set('search', search);
    if (departmentId) params = params.set('departmentId', departmentId.toString());
    if (status) params = params.set('status', status);
    return this.http.get<EmployeePage>(this.apiUrl, { params });
  }

  getEmployee(id: number): Observable<EmployeeDto> {
    return this.http.get<EmployeeDto>(`${this.apiUrl}/${id}`);
  }

  createEmployee(data: any): Observable<EmployeeDto> {
    return this.http.post<EmployeeDto>(this.apiUrl, data);
  }

  updateEmployee(id: number, data: any): Observable<EmployeeDto> {
    return this.http.put<EmployeeDto>(`${this.apiUrl}/${id}`, data);
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  createWithUser(data: any): Observable<EmployeeDto> {
    return this.http.post<EmployeeDto>(`${this.apiUrl}/with-user`, data);
  }
}
