import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

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

  deleteUser(id: number, username: string) {
    // Prevent admin from deleting themselves
    if (username === this.getCurrentUsername()) {
      Swal.fire({
        title: 'Not Allowed',
        text: 'You cannot delete your own account.',
        icon: 'warning',
        confirmButtonText: 'OK'
      });
      return;
    }

    // Show confirmation dialog
    Swal.fire({
      title: 'Delete User',
      html: `
        <div style="text-align: left; margin: 10px 0;">
          <strong>User:</strong> ${username}
        </div>
        <p style="color: #dc3545;">This action cannot be undone!</p>
        <p>Are you sure you want to delete this user?</p>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, Delete',
      cancelButtonText: 'Cancel',
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d'
    }).then((result) => {
      if (result.isConfirmed) {
        this.performDeleteUser(id);
      }
    });
  }

  private performDeleteUser(id: number): void {
    this.auth.deleteUser(id).subscribe({
      next: (response) => {
        console.log('Delete user response:', response);
        // Check if the response status indicates success
        if (response.status >= 200 && response.status < 300) {
          this.loadUsers(); // Only refresh the user list, no top message
          Swal.fire({
            title: 'Deleted!',
            text: 'User has been deleted successfully.',
            icon: 'success',
            timer: 3000,
            showConfirmButton: false
          });
        } else {
          throw new Error('Unexpected response status');
        }
      },
      error: (err) => {
        console.error('Error deleting user:', err);
        Swal.fire({
          title: 'Error!',
          text: 'Failed to delete user. Please try again.',
          icon: 'error',
          confirmButtonText: 'OK'
        });
      }
    });
  }

  changeUserRole(userId: number, newRole: string, username: string): void {
    // Prevent admin from changing their own role
    if (username === this.getCurrentUsername()) {
      Swal.fire({
        title: 'Not Allowed',
        text: 'You cannot change your own role.',
        icon: 'warning',
        confirmButtonText: 'OK'
      });
      return;
    }

    // Show confirmation dialog
    Swal.fire({
      title: 'Change User Role',
      html: `
        <div style="text-align: left; margin: 10px 0;">
          <strong>User:</strong> ${username}<br>
          <strong>New Role:</strong> ${newRole}
        </div>
        <p>Are you sure you want to change this user's role?</p>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Yes, Change Role',
      cancelButtonText: 'Cancel',
      confirmButtonColor: newRole === 'ADMIN' ? '#28a745' : '#6c757d',
      cancelButtonColor: '#dc3545'
    }).then((result) => {
      if (result.isConfirmed) {
        this.updateRole(userId, newRole);
      }
    });
  }

  getCurrentUsername(): string {
    return this.auth.getUsername();
  }

  updateRole(id: number, role: string) {
    this.auth.updateUserRole(id, role).subscribe({
      next: (response) => {
        console.log('Role update response:', response);
        // Check if the response status indicates success
        if (response.status >= 200 && response.status < 300) {
          this.loadUsers(); // Only refresh the user list, no top message
          Swal.fire({
            title: 'Success!',
            text: `User role updated to ${role} successfully!`,
            icon: 'success',
            timer: 3000,
            showConfirmButton: false
          });
        } else {
          throw new Error('Unexpected response status');
        }
      },
      error: (err) => {
        console.error('Error updating user role:', err);
        console.error('Error status:', err.status);
        console.error('Error message:', err.message);
        
        Swal.fire({
          title: 'Error!',
          text: 'Failed to update user role. Please try again.',
          icon: 'error',
          confirmButtonText: 'OK'
        });
      }
    });
  }
}
