import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { BranchService } from '../../services/branch.service';
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
  branches: any[] = [];
  error = '';
  success = '';

  constructor(
    private auth: AuthService,
    private branchService: BranchService
  ) {}

  ngOnInit() {
    this.loadUsers();
    this.loadBranches();
  }

  loadUsers() {
    // console.log('Loading users...');
    // console.log('Auth token:', this.auth.getToken());
    // console.log('User role:', this.auth.getRole());
    
    this.auth.getAllUsers().subscribe({
      next: (data) => {
        // console.log('Users loaded successfully:', data);
        this.users = data;
        this.error = '';
      },
      error: (err) => {
        // console.error('Error loading users:', err);
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
        // console.log('Delete user response:', response);
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
        // console.error('Error deleting user:', err);
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
        // console.log('Role update response:', response);
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
        // console.error('Error updating user role:', err);
        // console.error('Error status:', err.status);
        // console.error('Error message:', err.message);
        
        Swal.fire({
          title: 'Error!',
          text: 'Failed to update user role. Please try again.',
          icon: 'error',
          confirmButtonText: 'OK'
        });
      }
    });
  }

  loadBranches() {
    this.branchService.getAllBranches().subscribe({
      next: (branches) => {
        this.branches = branches;
      },
      error: (err) => {
        console.error('Error loading branches:', err);
      }
    });
  }

  showAddAdminModal() {
    Swal.fire({
      title: '<h3 style="color: var(--coop-red-600);">Add Admin User</h3>',
      html: `
        <div style="text-align: left;">
          <div style="margin-bottom: 1rem;">
            <label for="adminName" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Full Name *</label>
            <input type="text" id="adminName" class="swal2-input" placeholder="Enter full name" style="width: 100%; margin: 0;">
          </div>
          <div style="margin-bottom: 1rem;">
            <label for="adminEmail" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Email *</label>
            <input type="email" id="adminEmail" class="swal2-input" placeholder="Enter email" style="width: 100%; margin: 0;">
          </div>
          <div style="margin-bottom: 1rem;">
            <label for="adminUsername" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Username *</label>
            <input type="text" id="adminUsername" class="swal2-input" placeholder="Enter username" style="width: 100%; margin: 0;">
          </div>
          <div style="margin-bottom: 1rem;">
            <label for="adminPassword" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Password *</label>
            <input type="password" id="adminPassword" class="swal2-input" placeholder="Enter password" style="width: 100%; margin: 0;">
          </div>
        </div>
      `,
      showCancelButton: true,
      confirmButtonText: '<i class="fas fa-user-shield"></i> Create Admin',
      cancelButtonText: '<i class="fas fa-times"></i> Cancel',
      confirmButtonColor: '#dc2626',
      cancelButtonColor: '#6b7280',
      width: '500px',
      preConfirm: () => {
        const name = (document.getElementById('adminName') as HTMLInputElement).value;
        const email = (document.getElementById('adminEmail') as HTMLInputElement).value;
        const username = (document.getElementById('adminUsername') as HTMLInputElement).value;
        const password = (document.getElementById('adminPassword') as HTMLInputElement).value;

        if (!name || !email || !username || !password) {
          Swal.showValidationMessage('Please fill in all fields');
          return false;
        }

        if (password.length < 6) {
          Swal.showValidationMessage('Password must be at least 6 characters');
          return false;
        }

        return { name, email, username, password };
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.createAdminUser(result.value);
      }
    });
  }

  showAddBranchUserModal() {
    const branchOptions = this.branches.map((branch, index) => 
      `<div class="dropdown-item" data-value="${branch.id}" data-index="${index}">${branch.brnName}</div>`
    ).join('');

    Swal.fire({
      title: '<h3 style="color: var(--coop-blue-600);">Add Branch User</h3>',
      html: `
        <div style="text-align: left;">
          <div style="margin-bottom: 1rem;">
            <label for="branchSelect" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Branch *</label>
            <div class="custom-dropdown" style="position: relative;">
              <div class="dropdown-input" id="branchSelect" style="
                width: 100%; 
                margin: 0; 
                cursor: pointer; 
                background: white;
                border: 1px solid #d1d5db;
                border-radius: 4px;
                padding: 0.5rem 2.5rem 0.5rem 0.75rem;
                font-size: 1rem;
                color: #6b7280;
                position: relative;
                display: flex;
                align-items: center;
                justify-content: space-between;
                min-height: 40px;
                box-sizing: border-box;
              " data-selected-id="">
                <span id="selectedBranchText">-- Select Branch --</span>
                <svg style="
                  width: 16px; 
                  height: 16px; 
                  color: #6b7280; 
                  position: absolute; 
                  right: 12px; 
                  pointer-events: none;
                  transition: transform 0.2s;
                " fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                </svg>
              </div>
              <div id="branchDropdown" class="dropdown-list" style="
                position: absolute;
                top: 100%;
                left: 0;
                right: 0;
                background: white;
                border: 1px solid #d1d5db;
                border-top: none;
                border-radius: 0 0 4px 4px;
                max-height: 200px;
                overflow-y: auto;
                z-index: 1000;
                display: none;
                box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
              ">
                ${branchOptions}
              </div>
            </div>
            <small style="color: #6b7280; font-size: 0.875rem;">Click to select a branch from the dropdown</small>
          <div style="margin-bottom: 1rem;">
            <label for="branchUserName" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Full Name *</label>
            <input type="text" id="branchUserName" class="swal2-input" placeholder="Enter full name" style="width: 100%; margin: 0;">
          </div>
          <div style="margin-bottom: 1rem;">
            <label for="branchUserEmail" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Email *</label>
            <input type="email" id="branchUserEmail" class="swal2-input" placeholder="Enter email" style="width: 100%; margin: 0;">
          </div>
          <div style="margin-bottom: 1rem;">
            <label for="branchUserUsername" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Username *</label>
            <input type="text" id="branchUserUsername" class="swal2-input" placeholder="Enter username" style="width: 100%; margin: 0;">
          </div>
          <div style="margin-bottom: 1rem;">
            <label for="branchUserPassword" style="display: block; margin-bottom: 0.5rem; font-weight: 500;">Password *</label>
            <input type="password" id="branchUserPassword" class="swal2-input" placeholder="Enter password" style="width: 100%; margin: 0;">
          </div>
          </div>
        </div>
        <style>
          .dropdown-item {
            padding: 10px 12px;
            cursor: pointer;
            border-bottom: 1px solid #f3f4f6;
            transition: all 0.2s ease;
            font-size: 14px;
            color: #374151;
          }
          .dropdown-item:hover {
            background-color: #2563eb;
            color: white;
          }
          .dropdown-item:last-child {
            border-bottom: none;
          }
          .dropdown-item.selected {
            background-color: #2563eb;
            color: white;
          }
          .custom-dropdown {
            position: relative;
          }
          .dropdown-input {
            transition: all 0.2s ease;
          }
          .dropdown-input:hover {
            border-color: #2563eb;
          }
          .dropdown-input.open {
            border-color: #2563eb;
            border-bottom-left-radius: 0;
            border-bottom-right-radius: 0;
          }
          .dropdown-input.open svg {
            transform: rotate(180deg);
          }
          .dropdown-list {
            scrollbar-width: thin;
            scrollbar-color: #c1c1c1 #f1f1f1;
          }
          .dropdown-list::-webkit-scrollbar {
            width: 6px;
          }
          .dropdown-list::-webkit-scrollbar-track {
            background: #f1f1f1;
          }
          .dropdown-list::-webkit-scrollbar-thumb {
            background: #c1c1c1;
            border-radius: 3px;
          }
          .dropdown-list::-webkit-scrollbar-thumb:hover {
            background: #a8a8a8;
          }
        </style>
      `,
      showCancelButton: true,
      confirmButtonText: '<i class="fas fa-user-plus"></i> Create Branch User',
      cancelButtonText: '<i class="fas fa-times"></i> Cancel',
      confirmButtonColor: '#2563eb',
      cancelButtonColor: '#6b7280',
      width: '500px',
      didOpen: () => {
        // Setup dropdown functionality
        const branchSelect = document.getElementById('branchSelect') as HTMLElement;
        const dropdown = document.getElementById('branchDropdown') as HTMLElement;
        const selectedText = document.getElementById('selectedBranchText') as HTMLElement;
        const dropdownItems = dropdown.querySelectorAll('.dropdown-item');

        // Toggle dropdown on input click
        branchSelect.addEventListener('click', (e) => {
          e.stopPropagation();
          const isVisible = dropdown.style.display === 'block';
          if (isVisible) {
            dropdown.style.display = 'none';
            branchSelect.classList.remove('open');
          } else {
            dropdown.style.display = 'block';
            branchSelect.classList.add('open');
          }
        });

        // Handle dropdown item selection
        dropdownItems.forEach(item => {
          item.addEventListener('click', () => {
            const value = item.getAttribute('data-value');
            const text = item.textContent;
            
            // Update display
            selectedText.textContent = text || '';
            selectedText.style.color = '#374151'; // Change to dark color when selected
            branchSelect.setAttribute('data-selected-id', value || '');
            
            // Update selected state
            dropdownItems.forEach(i => i.classList.remove('selected'));
            item.classList.add('selected');
            
            // Hide dropdown
            dropdown.style.display = 'none';
            branchSelect.classList.remove('open');
          });
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', () => {
          dropdown.style.display = 'none';
          branchSelect.classList.remove('open');
        });

        // Prevent dropdown from closing when clicking on it
        dropdown.addEventListener('click', (e) => {
          e.stopPropagation();
        });
      },
      preConfirm: () => {
        const name = (document.getElementById('branchUserName') as HTMLInputElement).value;
        const email = (document.getElementById('branchUserEmail') as HTMLInputElement).value;
        const username = (document.getElementById('branchUserUsername') as HTMLInputElement).value;
        const password = (document.getElementById('branchUserPassword') as HTMLInputElement).value;
        const branchSelect = document.getElementById('branchSelect') as HTMLElement;
        const branchId = branchSelect.getAttribute('data-selected-id');

        if (!name || !email || !username || !password || !branchId) {
          Swal.showValidationMessage('Please fill in all fields and select a branch');
          return false;
        }

        if (password.length < 6) {
          Swal.showValidationMessage('Password must be at least 6 characters');
          return false;
        }

        return { name, email, username, password, branchId: parseInt(branchId) };
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.createBranchUser(result.value);
      }
    });
  }

  createAdminUser(userData: any) {
    const adminData = {
      name: userData.name,
      email: userData.email,
      username: userData.username,
      password: userData.password
    };

    this.auth.createAdmin(adminData).subscribe({
      next: (response) => {
        Swal.fire({
          title: 'Success!',
          text: 'Admin user created successfully!',
          icon: 'success',
          timer: 3000,
          showConfirmButton: false
        });
        this.loadUsers(); // Refresh the user list
      },
      error: (err) => {
        // console.error('Error creating admin:', err);
        Swal.fire({
          title: 'Error!',
          text: err.error?.error || 'Failed to create admin user. Please try again.',
          icon: 'error',
          confirmButtonText: 'OK'
        });
      }
    });
  }

  createBranchUser(userData: any) {
    const branchUserData = {
      name: userData.name,
      email: userData.email,
      username: userData.username,
      password: userData.password,
      branchId: userData.branchId
    };

    this.auth.createBranchUser(branchUserData).subscribe({
      next: (response) => {
        Swal.fire({
          title: 'Success!',
          text: 'Branch user created successfully!',
          icon: 'success',
          timer: 3000,
          showConfirmButton: false
        });
        this.loadUsers(); // Refresh the user list
      },
      error: (err) => {
        console.error('Error creating branch user:', err);
        Swal.fire({
          title: 'Error!',
          text: err.error?.error || 'Failed to create branch user. Please try again.',
          icon: 'error',
          confirmButtonText: 'OK'
        });
      }
    });
  }
}
