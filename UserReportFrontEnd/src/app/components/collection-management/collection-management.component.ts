import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RegionService, Region } from '../../services/region.service';
import { BranchService, BranchResponse } from '../../services/branch.service';
import { TargetService } from '../../services/target.service';
import { CollectionService, CollectionSaveRequest } from '../../services/collection.service';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-collection-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './collection-management.component.html',
  styleUrl: './collection-management.component.css'
})
export class CollectionManagementComponent implements OnInit {
  regions: Region[] = [];
  branches: BranchResponse[] = [];
  
  selectedRegionId: number | null = null;
  selectedBranchId: number | null = null;
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number = new Date().getMonth() + 1;
  selectedYearMonth: string = `${new Date().getFullYear()}-${String(new Date().getMonth() + 1).padStart(2, '0')}`;
  collectionAmount: number = 0;
  targetAmount: number = 0;

  // Update functionality properties
  existingCollection: any = null;
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
  loadingTarget = false;
  message = '';
  messageType: 'success' | 'error' = 'success';

  constructor(
    private regionService: RegionService,
    private branchService: BranchService,
    private targetService: TargetService,
    private collectionService: CollectionService,
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
    // Debug authentication
    this.authService.debugTokenInfo();
    
    // Check if user is logged in
    console.log('Collection component initializing...');
    
    if (!this.authService.isLoggedIn()) {
      console.error('User not logged in, redirecting to login');
      this.showMessage('Please login to access this page', 'error');
      this.router.navigate(['/login']);
      return;
    }

    console.log('User is authenticated, proceeding to load regions');
    
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
      this.targetAmount = 0;
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
    if (this.selectedBranchId && this.selectedYear && this.selectedMonth) {
      this.loadTarget();
      this.checkExistingCollection();
    }
  }

  onYearMonthChange(): void {
    // Parse the year-month string to update individual year and month values
    if (this.selectedYearMonth) {
      const [year, month] = this.selectedYearMonth.split('-');
      this.selectedYear = parseInt(year);
      this.selectedMonth = parseInt(month);
      
      // Load target and check for existing collection
      if (this.selectedBranchId) {
        this.loadTarget();
        this.checkExistingCollection();
      }
    }
  }

  checkExistingCollection(): void {
    if (!this.selectedBranchId || !this.selectedYear || !this.selectedMonth) {
      return;
    }

    this.collectionService.getCollectionByBranchAndYearMonth(
      this.selectedBranchId, this.selectedYear, this.selectedMonth
    ).subscribe({
      next: (collection) => {
        console.log('Existing collection found:', collection);
        this.existingCollection = collection;
        this.isUpdateMode = true;
        this.collectionAmount = collection.collection;
        
        const monthName = this.months.find(m => m.value === this.selectedMonth)?.name;
        this.showMessage(
          `Collection already exists for ${monthName} ${this.selectedYear}: ${collection.collection}`, 
          'error'
        );
      },
      error: (error) => {
        console.log('No existing collection found or error:', error);
        this.existingCollection = null;
        this.isUpdateMode = false;
        this.collectionAmount = 0;
        this.message = '';
      }
    });
  }

  loadTarget(): void {
    if (!this.selectedBranchId || !this.selectedYear || !this.selectedMonth) {
      return;
    }

    this.loadingTarget = true;
    this.targetService.getTargetsByBranchAndYearMonth(
      this.selectedBranchId, 
      this.selectedYear, 
      this.selectedMonth
    ).subscribe({
      next: (target) => {
        this.targetAmount = target.target;
        this.loadingTarget = false;
      },
      error: (error) => {
        console.error('No target found for this selection:', error);
        this.targetAmount = 0;
        this.loadingTarget = false;
        this.showMessage('No target found for the selected branch, year, and month', 'error');
      }
    });
  }

  getAchievementPercentage(): number {
    if (this.targetAmount === 0) return 0;
    return Math.round((this.collectionAmount / this.targetAmount) * 100);
  }

  getAchievementStatus(): string {
    const percentage = this.getAchievementPercentage();
    if (percentage >= 100) return 'excellent';
    if (percentage >= 80) return 'good';
    if (percentage >= 60) return 'moderate';
    return 'poor';
  }

  canSubmit(): boolean {
    return this.selectedRegionId !== null && 
           this.selectedBranchId !== null && 
           this.collectionAmount > 0 && 
           this.selectedYear > 0 && 
           this.selectedMonth > 0 &&
           this.targetAmount > 0;
  }

  submitCollection(): void {
    if (!this.canSubmit()) {
      this.showMessage('Please fill all required fields and ensure a target exists', 'error');
      return;
    }

    // If in update mode and user is not admin, prevent update
    if (this.isUpdateMode && !this.isAdmin) {
      this.showMessage('Only administrators can update collections', 'error');
      return;
    }

    // If in update mode, show confirmation dialog
    if (this.isUpdateMode) {
      this.showUpdateConfirmation();
      return;
    }

    // Regular create operation
    this.performCreateCollection();
  }

  showUpdateConfirmation(): void {
    const monthName = this.months.find(m => m.value === this.selectedMonth)?.name;
    const branchName = this.branches.find(b => b.id === this.selectedBranchId)?.brnName;
    
    Swal.fire({
      title: 'Update Collection',
      html: `
        <div style="text-align: left; margin: 20px 0;">
          <p><strong>Branch:</strong> ${branchName}</p>
          <p><strong>Period:</strong> ${monthName} ${this.selectedYear}</p>
          <p><strong>Current Amount:</strong> Rs. ${this.existingCollection?.collectionAmount?.toLocaleString() || 0}</p>
          <p><strong>New Amount:</strong> Rs. ${this.collectionAmount.toLocaleString()}</p>
        </div>
        <p>Are you sure you want to update this collection?</p>
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
        this.performUpdateCollection();
      }
    });
  }

  performCreateCollection(): void {
    this.loading = true;
    
    const collectionRequest: CollectionSaveRequest = {
      collectionAmount: this.collectionAmount,
      collectionYear: this.selectedYear,
      collectionMonth: this.selectedMonth,
      branchId: this.selectedBranchId!
    };

    console.log('Creating collection request:', collectionRequest);

    this.collectionService.createCollection(collectionRequest).subscribe({
      next: (response) => {
        console.log('Collection creation response:', response);
        
        // Check if the response contains an error message
        if (response.error) {
          this.showMessage(response.error, 'error');
        } else {
          this.showMessage('Collection recorded successfully!', 'success');
          this.resetForm();
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error creating collection:', error);
        console.error('Error details:', error.error);
        this.showMessage('Error recording collection. Please try again.', 'error');
        this.loading = false;
      }
    });
  }

  performUpdateCollection(): void {
    if (!this.existingCollection) {
      this.showMessage('No existing collection to update', 'error');
      return;
    }

    this.loading = true;
    
    // Calculate due amount: target - collection
    const dueAmount = this.targetAmount - this.collectionAmount;
    
    const updateRequest = {
      target: this.targetAmount,
      due: dueAmount,
      collectionAmount: this.collectionAmount,
      collectionYear: this.selectedYear,
      collectionMonth: this.selectedMonth
    };

    this.collectionService.updateCollection(this.existingCollection.id, updateRequest).subscribe({
      next: (response) => {
        console.log('Collection update response:', response);
        
        // Check if the response contains an error message
        if (response.error) {
          this.showMessage(response.error, 'error');
        } else {
          this.showMessage('Collection updated successfully!', 'success');
          this.resetForm();
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error updating collection:', error);
        this.showMessage('Error updating collection. Please try again.', 'error');
        this.loading = false;
      }
    });
  }

  resetForm(): void {
    this.selectedRegionId = null;
    this.selectedBranchId = null;
    this.branches = [];
    this.collectionAmount = 0;
    this.targetAmount = 0;
    this.selectedYear = new Date().getFullYear();
    this.selectedMonth = new Date().getMonth() + 1;
    this.existingCollection = null;
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
