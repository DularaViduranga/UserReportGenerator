import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  formData = { username: '', password: '' };
  error = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  onSubmit() {
    this.loading = true;
    this.error = '';
    this.auth.login(this.formData).subscribe({
      next: (res) => {
        console.log('Login response:', res);
        if (res.token) {
          if (typeof localStorage !== 'undefined') {
            localStorage.setItem('authToken', res.token);
            console.log('Token stored in localStorage');
            console.log('Token value:', res.token);
          }
          this.router.navigate(['/dashboard']);
        } else {
          this.error = res.error || 'Login failed';
        }
      },
      error: () => {
        this.error = 'Invalid credentials';
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}