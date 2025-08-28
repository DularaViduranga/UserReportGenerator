import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RegionService, Region } from '../../services/region.service';
import { BranchService, BranchResponse } from '../../services/branch.service';
import { TargetService, TargetSaveRequest } from '../../services/target.service';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-target-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './target-management.component.html',
  styleUrl: './target-management.component.css'
})
export class TargetManagementComponent implements OnInit {
  regions: Region[] = [];
  branches: BranchResponse[] = [];
  
  selectedRegionId: number | null = null;
  selectedBranchId: number | null = null;
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1;
  selectedYearMonth: string = `${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, '0')}`;
  targetAmount: number = 0;

  // Update functionality properties
  existingTarget: any = null;
  isUpdateMode: boolean = false;
  isAdmin: boolean = false;

  years: number[] = [];
  months = [
    { value: 1, name: 'January' },
    { value: 2, name: 'February' },
    { value: 3, name: 'March' },
    { value: 4, name: 'April' },
    { value: 5, name: 'May' },
    { value: 6, name: 'June' },
    { value: 7, name: 'July' },
    { value: 8, name: 'August' },
    { value: 9, name: 'September' },
    { value: 10, name: 'October' },
    { value: 11, name: 'November' },
    { value: 12, name: 'December' }
  ];

  loading = false;
  message = '';
  messageType: 'success' | 'error' = 'success';

  constructor(
    private regionService: RegionService,
    private branchService: BranchService,
    private targetService: TargetService,
    private authService: AuthService,
    private router: Router
  ) {
    // Generate years (current year - 5 to current year + 5)
    const currentYear = new Date().getFullYear();
    for (let i = currentYear - 5; i <= currentYear + 5; i++) {
      this.years.push(i);
    }
  }

  ngOnInit(): void {
    // Check if user is logged in
    if (!this.authService.isLoggedIn()) {
      console.error('User not logged in, redirecting to login');
      this.router.navigate(['/login']);
      return;
    }

    console.log('User is logged in, token exists:', !!this.authService.getToken());
    
    // Check if user is admin
    this.isAdmin = this.authService.isAdmin();
    console.log('User is admin:', this.isAdmin);
    
    this.loadRegions();
  }

  loadRegions(): void {
    console.log('Loading regions...');
    this.regionService.getAllRegions().subscribe({
      next: (regions) => {
        console.log('Regions loaded successfully:', regions);
        this.regions = regions;
      },
      error: (error) => {
        console.error('Error loading regions:', error);
        if (error.status === 403) {
          console.error('Authentication failed - token expired or invalid');
          this.showMessage('Authentication failed. Please login again.', 'error');
          this.authService.logout();
          this.router.navigate(['/login']);
        } else {
          this.showMessage('Error loading regions: ' + (error.message || 'Unknown error'), 'error');
        }
      }
    });
  }

  onRegionChange(): void {
    if (this.selectedRegionId) {
      console.log('Loading branches for region ID:', this.selectedRegionId);
      this.branches = [];
      this.selectedBranchId = null;
      this.branchService.getBranchesByRegion(this.selectedRegionId).subscribe({
        next: (branches) => {
          console.log('Branches loaded successfully:', branches);
          this.branches = branches;
        },
        error: (error) => {
          console.error('Error loading branches:', error);
          console.error('Error status:', error.status);
          console.error('Error message:', error.message);
          this.showMessage('Error loading branches', 'error');
        }
      });
    }
  }

  onSelectionChange(): void {
    // Check for existing target when branch/month/year selection changes
    if (this.selectedBranchId && this.selectedYear && this.selectedMonth) {
      this.checkExistingTarget();
    }
  }

  onYearMonthChange(): void {
    // Parse the year-month string to update individual year and month values
    if (this.selectedYearMonth) {
      const [year, month] = this.selectedYearMonth.split('-');
      this.selectedYear = parseInt(year);
      this.selectedMonth = parseInt(month);
      
      // Check for existing target
      if (this.selectedBranchId) {
        this.checkExistingTarget();
      }
    }
  }

  checkExistingTarget(): void {
    if (!this.selectedBranchId || !this.selectedYear || !this.selectedMonth) {
      return;
    }

    this.targetService.getTargetsByBranchAndYearMonth(
      this.selectedBranchId, this.selectedYear, this.selectedMonth
    ).subscribe({
      next: (target) => {
        console.log('Existing target found:', target);
        this.existingTarget = target;
        this.isUpdateMode = true;
        // Keep input field empty for update mode
        this.targetAmount = 0;
        // Clear any error messages
        this.message = '';
      },
      error: (error) => {
        console.log('No existing target found or error:', error);
        this.existingTarget = null;
        this.isUpdateMode = false;
        this.targetAmount = 0;
        this.message = '';
      }
    });
  }

  canSubmit(): boolean {
    return this.selectedRegionId !== null && 
           this.selectedBranchId !== null && 
           this.targetAmount > 0 && 
           this.selectedYear > 0 && 
           this.selectedMonth > 0;
  }

  submitTarget(): void {
    if (!this.canSubmit()) {
      this.showMessage('Please fill all required fields', 'error');
      return;
    }

    // If in update mode and user is not admin, prevent update
    if (this.isUpdateMode && !this.isAdmin) {
      this.showMessage('Only administrators can update targets', 'error');
      return;
    }

    // If in update mode, show confirmation dialog
    if (this.isUpdateMode) {
      this.showUpdateConfirmation();
      return;
    }

    // Regular create operation
    this.performCreateTarget();
  }

  showUpdateConfirmation(): void {
    const monthName = this.months.find(m => m.value === this.selectedMonth)?.name;
    const branchName = this.branches.find(b => b.id === this.selectedBranchId)?.brnName;
    
    Swal.fire({
      title: 'Update Target',
      html: `
        <div style="text-align: left; margin: 20px 0;">
          <p><strong>Branch:</strong> ${branchName}</p>
          <p><strong>Period:</strong> ${monthName} ${this.selectedYear}</p>
          <p><strong>Current Target:</strong> Rs. ${this.existingTarget?.target?.toLocaleString() || 0}</p>
          <p><strong>New Target:</strong> Rs. ${this.targetAmount.toLocaleString()}</p>
        </div>
        <p>Are you sure you want to update this target?</p>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#ffc107',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Yes, Update',
      cancelButtonText: 'Cancel',
      background: '#fff',
      customClass: {
        popup: 'swal-popup',
        title: 'swal-title',
        htmlContainer: 'swal-html'
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.performUpdateTarget();
      }
    });
  }

  performCreateTarget(): void {
    this.loading = true;
    
    const targetRequest: TargetSaveRequest = {
      target: this.targetAmount,
      targetYear: this.selectedYear,
      targetMonth: this.selectedMonth,
      branchId: this.selectedBranchId!
    };

    this.targetService.createTarget(targetRequest).subscribe({
      next: (response) => {
        this.showMessage('Target set successfully!', 'success');
        this.resetForm();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error creating target:', error);
        this.showMessage('Error setting target. Please try again.', 'error');
        this.loading = false;
      }
    });
  }

  performUpdateTarget(): void {
    if (!this.existingTarget) {
      this.showMessage('No existing target to update', 'error');
      return;
    }

    this.loading = true;
    
    const updateRequest = {
      target: this.targetAmount,
      targetYear: this.selectedYear,
      targetMonth: this.selectedMonth,
      branchId: this.selectedBranchId!
    };

    this.targetService.updateTarget(this.existingTarget.id, updateRequest).subscribe({
      next: (response) => {
        this.showMessage('Target updated successfully!', 'success');
        this.resetForm();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error updating target:', error);
        this.showMessage('Error updating target. Please try again.', 'error');
        this.loading = false;
      }
    });
  }

  resetForm(): void {
    this.selectedRegionId = null;
    this.selectedBranchId = null;
    this.branches = [];
    this.targetAmount = 0;
    this.selectedYear = new Date().getFullYear();
    this.selectedMonth = new Date().getMonth() + 1;
    this.existingTarget = null;
    this.isUpdateMode = false;
    this.message = '';
  }

  showMessage(text: string, type: 'success' | 'error'): void {
    // Still set the message for any components that might need it
    this.message = text;
    this.messageType = type;

    // Show SweetAlert2 notification
    Swal.fire({
      title: type === 'success' ? 'Success!' : 'Error!',
      text: text,
      icon: type === 'success' ? 'success' : 'error',
      confirmButtonColor: type === 'success' ? '#28a745' : '#dc3545',
      confirmButtonText: 'OK',
      timer: type === 'success' ? 3000 : undefined,
      timerProgressBar: type === 'success' ? true : false,
      background: '#fff',
      customClass: {
        popup: 'swal-popup',
        title: 'swal-title'
      }
    });

    // Clear message after delay
    setTimeout(() => {
      this.message = '';
    }, 5000);
  }
}
