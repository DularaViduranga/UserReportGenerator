import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RegionService, Region } from '../../services/region.service';
import { BranchService, BranchResponse } from '../../services/branch.service';
import { TargetService } from '../../services/target.service';
import { CollectionService, CollectionSaveRequest } from '../../services/collection.service';
import { AuthService } from '../../services/auth.service';
import { ExcelImportComponent } from '../target-management/excel-import/excel-import.component';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-collection-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ExcelImportComponent],
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
    public authService: AuthService,
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

    console.log('User is authenticated, proceeding to initialize');
    
    // Check if user is admin
    this.isAdmin = this.authService.isAdminUser();
    console.log('User is admin:', this.isAdmin);
    
    if (this.isAdmin) {
      // Admin can see all regions and branches
      this.loadRegions();
    } else {
      // Branch user - auto-select their branch
      this.autoSelectUserBranch();
    }
  }

  private autoSelectUserBranch(): void {
    const userBranchId = this.authService.getUserBranchId();
    const userBranchName = this.authService.getUserBranchName();
    
    if (userBranchId && userBranchName) {
      console.log(`Auto-selecting branch for user: ${userBranchName} (ID: ${userBranchId})`);
      
      this.selectedBranchId = userBranchId;
      // You might need to load the branch details to get region info
      // For now, we'll trigger the selection change
      this.onSelectionChange();
    } else {
      this.showMessage('Unable to determine your branch. Please contact administrator.', 'error');
    }
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
        
        const monthName = this.months.find(m => m.value === this.selectedMonth)?.name;
        
        if (this.isAdmin) {
          // For admin users - no error, just switch to update mode silently
          console.log('Admin user - switching to update mode');
          this.collectionAmount = 0; // Keep input field empty for new amount
          this.message = ''; // Clear any error messages
        } else {
          // For normal users - show error since they can't update
          this.collectionAmount = collection.collection;
          this.showMessage(
            `Collection already exists for ${monthName} ${this.selectedYear}. Only administrators can update existing collections.`, 
            'error'
          );
        }
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
    // For admin users, region selection is required
    // For regular users, only branch selection is required (auto-selected)
    const regionRequirement = this.isAdmin ? (this.selectedRegionId !== null) : true;
    
    return regionRequirement && 
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
    // Ensure proper type conversion for comparison
    const branchIdToFind = typeof this.selectedBranchId === 'string' ? 
      parseInt(this.selectedBranchId) : this.selectedBranchId;
    const selectedBranch = this.branches.find(b => b.id === branchIdToFind);
    const branchName = selectedBranch?.brnName || 'Unknown Branch';
    
    console.log('Update confirmation - selectedBranchId:', this.selectedBranchId, 'type:', typeof this.selectedBranchId);
    console.log('Update confirmation - branchIdToFind:', branchIdToFind);
    console.log('Update confirmation - branches:', this.branches);
    console.log('Update confirmation - selectedBranch:', selectedBranch);
    console.log('Update confirmation - branchName:', branchName);
    
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
          // Refresh the collection data to show updated values in real-time
          this.refreshCollectionDataAfterCreate();
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
          // Refresh the collection data to show updated values in real-time
          this.refreshCollectionData();
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

  refreshCollectionData(): void {
    // Keep the form values intact and refresh the data displays
    // This is similar to what Target Management does after update
    
    if (this.selectedBranchId && this.selectedYear && this.selectedMonth) {
      // Refresh target amount for the selected period
      this.loadTarget();
      
      // Refresh existing collection data
      this.checkExistingCollection();
      
      // Reset only the collection amount input to 0 for next entry
      this.collectionAmount = 0;
    }
  }

  refreshCollectionDataAfterCreate(): void {
    // Refresh data after successful creation without showing error messages
    if (this.selectedBranchId && this.selectedYear && this.selectedMonth) {
      // Refresh target amount for the selected period
      this.loadTarget();
      
      // Silently check for existing collection without showing error messages
      this.collectionService.getCollectionByBranchAndYearMonth(
        this.selectedBranchId, this.selectedYear, this.selectedMonth
      ).subscribe({
        next: (collection) => {
          console.log('Collection refreshed after create:', collection);
          this.existingCollection = collection;
          this.isUpdateMode = true;
          // Don't show any error messages - just silently update the state
        },
        error: (error) => {
          console.log('No existing collection found after create:', error);
          this.existingCollection = null;
          this.isUpdateMode = false;
        }
      });
      
      // Reset only the collection amount input to 0 for next entry
      this.collectionAmount = 0;
    }
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

  async onUploadCollectionExcel(data: {year: number, month: number, file: File}): Promise<void> {
    // First check if targets exist for this period
    try {
      const existingTargets = await this.targetService.getTargetsByYearAndMonth(data.year, data.month).toPromise();
      
      if (!existingTargets || existingTargets.length === 0) {
        await Swal.fire({
          icon: 'error',
          title: 'No Targets Found',
          text: 'Please add targets first before adding collections.',
          confirmButtonColor: '#dc3545'
        });
        return;
      }

      // Check if there are existing collections for this period
      const existingCheck = await this.collectionService.checkExistingCollections(data.year, data.month).toPromise();
      
      if (existingCheck && existingCheck.hasExistingCollections) {
        // Show confirmation dialog if collections already exist
        const confirmResult = await Swal.fire({
          title: '<h3 style="color: #ffc107;">⚠️ Existing Collections Found</h3>',
          html: `
            <div style="text-align: center;">
              <p>There are already existing collections for <strong>${this.getMonthName(data.month)} ${data.year}</strong></p>
              <div style="background: #fff3cd; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #ffc107;">
                <strong>📊 Existing Collections:</strong> ${existingCheck.count || 0} collection(s) found
              </div>
              <p style="color: #856404;">
                Uploading this Excel file will <strong>overwrite</strong> the existing collections.
              </p>
              <p style="color: #6c757d; font-size: 0.9rem;">
                Are you sure you want to continue?
              </p>
            </div>
          `,
          icon: 'warning',
          showCancelButton: true,
          confirmButtonText: '<i class="fas fa-upload"></i> Yes, Overwrite',
          cancelButtonText: '<i class="fas fa-times"></i> Cancel',
          confirmButtonColor: '#ffc107',
          cancelButtonColor: '#6c757d',
          width: '500px'
        });

        if (!confirmResult.isConfirmed) {
          return; // User cancelled
        }
      }
    } catch (error) {
      console.log('No existing collections check endpoint or error:', error);
      // Continue with upload if check fails
    }

    this.uploadCollectionExcelFile(data.year, data.month, data.file);
  }

  private async uploadCollectionExcelFile(year: number, month: number, file: File): Promise<void> {
    // Show loading
    Swal.fire({
      title: 'Uploading...',
      html: `
        <div style="text-align: center;">
          <div style="margin-bottom: 15px;">
            <img src="https://icons.veryicon.com/png/o/application/skills-section/microsoft-excel-10.png" 
                 style="width: 50px; height: 50px;">
          </div>
          <p>Processing Excel file for ${this.getMonthName(month)} ${year}</p>
          <p style="color: #6c757d; font-size: 0.9rem;">Please wait while we import your collections...</p>
        </div>
      `,
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    try {
      const response = await this.collectionService.uploadExcelCollections(year, month, file).toPromise();
      
      await Swal.fire({
        icon: 'success',
        title: 'Upload Successful!',
        html: `
          <div style="text-align: center;">
            <p>Excel file has been processed successfully!</p>
            <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0;">
              <strong>📅 Period:</strong> ${this.getMonthName(month)} ${year}<br>
              <strong>📄 File:</strong> ${file.name}
            </div>
            <p style="color: #28a745; background: #d4edda; padding: 10px; border-radius: 5px;">
              ${response || 'Collections uploaded successfully!'}
            </p>
          </div>
        `,
        confirmButtonColor: '#28a745',
        timer: 4000,
        timerProgressBar: true
      });

      // Refresh data if we're viewing the same period
      if (this.selectedYear === year && this.selectedMonth === month && this.selectedBranchId) {
        this.checkExistingCollection();
      }

    } catch (error: any) {
      console.error('Error uploading Excel file:', error);
      
      const originalErrorMessage = error.error || error.message || 'Unknown error occurred';
      const isFileNameError = originalErrorMessage.toLowerCase().includes('sheet') && originalErrorMessage.toLowerCase().includes('null');
      const isExistingDataError = originalErrorMessage.toLowerCase().includes('already exist');
      
      let errorMessage = originalErrorMessage;
      if (isFileNameError) {
        errorMessage = 'Failed to upload collections: The uploaded sheet name is not allowed. Only sheets named \'Collections\' or \'collections\' are accepted.';
      }
      
      await Swal.fire({
        icon: 'error',
        title: 'Upload Failed',
        html: `
          <div style="text-align: center;">
            <p>Failed to upload Excel file</p>
            <div style="background: #fff2f2; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #dc3545;">
              <strong>Error:</strong> ${errorMessage}
            </div>
          </div>
        `,
        confirmButtonColor: '#dc3545'
      });
    }
  }

  private getMonthName(month: number): string {
    const monthNames = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return monthNames[month - 1] || '';
  }

  onUpdateCollectionExcel(data: {year: number, month: number, file: File}): void {
    // Check for existing collections before updating
    this.collectionService.checkExistingCollections(data.year, data.month).subscribe({
      next: (response) => {
        if (response.hasExistingCollections) {
          this.showExcelUpdateConfirmation(response.count, data);
        } else {
          Swal.fire({
            title: 'No Collections Found',
            text: `No existing collections found for ${this.getMonthName(data.month)} ${data.year} to update.`,
            icon: 'warning',
            confirmButtonText: 'OK'
          });
        }
      },
      error: (error) => {
        console.error('Error checking existing collections:', error);
        this.updateCollectionExcelFile(data);
      }
    });
  }

  private showExcelUpdateConfirmation(count: number, data: {year: number, month: number, file: File}): void {
    Swal.fire({
      title: 'Update Existing Collections',
      html: `
        <p>Found <strong>${count}</strong> existing collections for ${this.getMonthName(data.month)} ${data.year}.</p>
        <p style="color: #f39c12;"><strong>Note:</strong> This will update the existing collection data.</p>
        <p>Do you want to continue?</p>
      `,
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: 'Yes, Update',
      cancelButtonText: 'Cancel',
      confirmButtonColor: '#f39c12'
    }).then((result) => {
      if (result.isConfirmed) {
        this.updateCollectionExcelFile(data);
      }
    });
  }

  private updateCollectionExcelFile(data: {year: number, month: number, file: File}): void {
    Swal.fire({
      title: 'Updating...',
      html: `Updating collections from <strong>${data.file.name}</strong> for ${this.getMonthName(data.month)} ${data.year}`,
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    this.collectionService.updateExcelCollections(data.year, data.month, data.file).subscribe({
      next: (response) => {
        Swal.fire({
          title: 'Update Successful!',
          text: response || 'Collections updated successfully!',
          icon: 'success',
          timer: 3000,
          showConfirmButton: false
        });
        
        // Refresh collections if viewing the same period
        if (this.selectedYear === data.year && this.selectedMonth === data.month && this.selectedBranchId) {
          this.onSelectionChange();
        }
      },
      error: (error) => {
        console.error('Error updating collections:', error);
        
        const originalErrorMessage = error.error || 'Failed to update collections.';
        const isFileNameError = originalErrorMessage.toLowerCase().includes('sheet') && originalErrorMessage.toLowerCase().includes('null');
        const isExistingDataError = originalErrorMessage.toLowerCase().includes('already exist');
        
        let finalMessage = originalErrorMessage;
        if (isFileNameError) {
          finalMessage = 'Failed to update collections: The uploaded sheet name is not allowed. Only sheets named \'Collections\' or \'collections\' are accepted.';
        }
        
        Swal.fire({
          title: 'Update Failed',
          text: finalMessage,
          icon: 'error'
        });
      }
    });
  }
}
