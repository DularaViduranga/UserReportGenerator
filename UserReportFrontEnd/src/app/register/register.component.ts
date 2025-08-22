// src/app/register/register.component.ts

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  formData = { name: '', email: '', username: '', password: '' };
  error = '';
  success = false;

  constructor(private auth: AuthService, private router: Router) {}

  onSubmit() {
    this.auth.register(this.formData).subscribe({
      next: (res) => {
        if (res.message) {
          this.success = true;
          setTimeout(() => this.router.navigate(['/login']), 2000);
        } else {
          this.error = res.error || 'Registration failed';
        }
      },
      error: () => {
        this.error = 'Registration failed';
      }
    });
  }
}