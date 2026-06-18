import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <div class="logo">
          <i class="ti ti-briefcase"></i>
          <h1>HR CORE</h1>
          <p>Нэвтрэх</p>
        </div>
        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
          <div class="field">
            <label>Хэрэглэгч</label>
            <input type="text" formControlName="username" placeholder="Хэрэглэгчийн нэр">
          </div>
          <div class="field">
            <label>Нууц үг</label>
            <input type="password" formControlName="password" placeholder="Нууц үг">
          </div>
          <p class="error" *ngIf="errorMessage">{{ errorMessage }}</p>
          <button type="submit" [disabled]="loginForm.invalid || loading">
            {{ loading ? 'Түр хүлээнэ үү...' : 'Нэвтрэх' }}
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #0f172a;
    }
    .login-card {
      background: #fff;
      border-radius: 1rem;
      padding: 2.5rem;
      width: 100%;
      max-width: 400px;
      box-shadow: 0 4px 24px rgba(0,0,0,0.15);
    }
    .logo { text-align: center; margin-bottom: 2rem; }
    .logo i { font-size: 2.5rem; color: #2563eb; }
    .logo h1 { margin: 0.5rem 0 0; font-size: 1.5rem; color: #0f172a; }
    .logo p { margin: 0.25rem 0 0; color: #64748b; font-size: 0.875rem; }
    .field { margin-bottom: 1.25rem; }
    .field label { display: block; font-size: 0.875rem; font-weight: 500; color: #334155; margin-bottom: 0.375rem; }
    .field input {
      width: 100%; padding: 0.75rem; border: 1px solid #e2e8f0; border-radius: 0.5rem;
      font-size: 0.875rem; outline: none; box-sizing: border-box; transition: border-color 0.2s;
    }
    .field input:focus { border-color: #2563eb; }
    .error { color: #ef4444; font-size: 0.8rem; margin: 0 0 1rem; text-align: center; }
    button {
      width: 100%; padding: 0.75rem; background: #2563eb; color: #fff; border: none;
      border-radius: 0.5rem; font-size: 0.9rem; font-weight: 600; cursor: pointer;
      transition: background 0.2s;
    }
    button:hover:not(:disabled) { background: #1d4ed8; }
    button:disabled { opacity: 0.6; cursor: not-allowed; }
  `]
})
export class LoginComponent {
  loginForm;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;
    this.loading = true;
    this.errorMessage = '';
    const { username, password } = this.loginForm.value;
    this.authService.login(username ?? '', password ?? '').subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Нэвтрэхэд алдаа гарлаа';
      }
    });
  }
}
