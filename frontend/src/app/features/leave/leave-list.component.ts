import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-leave-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="padding:2rem">
      <h1>Чөлөө</h1>
      <p>Leave management page (placeholder)</p>
    </div>
  `
})
export class LeaveListComponent {}
