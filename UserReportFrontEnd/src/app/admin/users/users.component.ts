import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class AdminUsersComponent implements OnInit {
  users: any[] = [];
  error = '';
  success = '';

  constructor(private auth: AuthService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    console.log('Loading users...');
    console.log('Auth token:', this.auth.getToken());
    console.log('User role:', this.auth.getRole());
    
    this.auth.getAllUsers().subscribe({
      next: (data) => {
        console.log('Users loaded successfully:', data);
        this.users = data;
        this.error = '';
      },
      error: (err) => {
        console.error('Error loading users:', err);
        this.error = 'Failed to load users: ' + (err.message || err.statusText || 'Unknown error');
      }
    });
  }

  deleteUser(id: number) {
    if (confirm('Are you sure?')) {
      this.auth.deleteUser(id).subscribe({
        next: () => {
          this.success = 'User deleted';
          this.loadUsers();
        },
        error: () => this.error = 'Delete failed'
      });
    }
  }

  updateRole(id: number, role: string) {
    this.auth.updateUserRole(id, role).subscribe({
      next: () => {
        this.success = 'Role updated';
        this.loadUsers();
      },
      error: () => this.error = 'Update failed'
    });
  }
}
