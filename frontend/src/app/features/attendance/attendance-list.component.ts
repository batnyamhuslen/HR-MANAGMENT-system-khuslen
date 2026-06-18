import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-attendance-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="padding:2rem">
      <h1>Ирц</h1>
      <p>Attendance page (placeholder)</p>
    </div>
  `
})
export class AttendanceListComponent {}
