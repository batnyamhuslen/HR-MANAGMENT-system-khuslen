import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div style="padding:2rem">
      <h1>Ажилтнууд</h1>
      <p>Employee management page (placeholder)</p>
    </div>
  `
})
export class EmployeeListComponent {}
