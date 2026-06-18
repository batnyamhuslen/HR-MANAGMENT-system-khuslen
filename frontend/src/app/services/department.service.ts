import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DepartmentDto {
  id: number;
  name: string;
  description: string;
}

@Injectable({ providedIn: 'root' })
export class DepartmentService {
  private apiUrl = 'http://127.0.0.1:8080/api/departments';

  constructor(private http: HttpClient) {}

  getAll(): Observable<DepartmentDto[]> {
    return this.http.get<DepartmentDto[]>(this.apiUrl);
  }
}
