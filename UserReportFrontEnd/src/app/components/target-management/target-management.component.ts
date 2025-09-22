import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RegionService, Region } from '../../services/region.service';
import { BranchService, BranchResponse } from '../../services/branch.service';
import { TargetService, TargetSaveRequest } from '../../services/target.service';
import { AuthService } from '../../services/auth.service';
import { TargetFormComponent } from './target-form/target-form.component';
import { ExcelImportComponent } from './excel-import/excel-import.component';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-target-management',
  standalone: true,
  imports: [CommonModule, FormsModule, TargetFormComponent, ExcelImportComponent],
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
  
  // Current target data
  existingTarget: any = null;
  isAdmin: boolean = false;
  loading: boolean = false;

  // User branch info for non-admin users
  userBranchId: number | null = null;
  userBranchName: string | null = null;

  // Computed property for selected branch name
  get selectedBranchName(): string {
    if (this.userBranchName && !this.isAdmin) {
      return this.userBranchName;
    }
    
    if (this.selectedBranchId && this.branches.length > 0) {
      const branch = this.branches.find(b => b.id === this.selectedBranchId);
      return branch ? branch.brnName : '';
    }
    
    return '';
  }

  constructor(
    private router: Router,
    private regionService: RegionService,
    private branchService: BranchService,
    private targetService: TargetService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.isAdmin = this.authService.isAdminUser();
    
    if (this.isAdmin) {
      this.loadRegions();
    } else {
      this.initializeBranchUser();
    }
  }

  loadRegions(): void {
    this.regionService.getAllRegions().subscribe({
      next: (response) => {
        this.regions = response;
      },
      error: (error) => {
        console.error('Error loading regions:', error);
        this.showMessage('Error loading regions', 'error');
      }
    });
  }

  initializeBranchUser(): void {
    this.userBranchId = this.authService.getUserBranchId();
    this.userBranchName = this.authService.getUserBranchName();
    
    if (this.userBranchId) {
      this.selectedBranchId = this.userBranchId;
      this.loadTargetForBranch();
    }
  }

  loadTargetForBranch(): void {
    if (!this.selectedBranchId || !this.selectedYear || !this.selectedMonth) {
      return;
    }

    this.loading = true;
    this.targetService.getTargetsByBranchAndYearMonth(
      this.selectedBranchId, 
      this.selectedYear, 
      this.selectedMonth
    ).subscribe({
      next: (target) => {
        this.existingTarget = target;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading target:', error);
        this.existingTarget = null;
        this.loading = false;
      }
    });
  }

  // Event handlers for child components
  onRegionChange(regionId: number): void {
    this.selectedRegionId = regionId;
    this.branches = [];
    this.existingTarget = null;
    
    this.branchService.getBranchesByRegion(regionId).subscribe({
      next: (response) => {
        this.branches = response;
      },
      error: (error) => {
        console.error('Error loading branches:', error);
        this.showMessage('Error loading branches', 'error');
      }
    });
  }

  onBranchChange(data: {branchId: number, regionId: number}): void {
    this.selectedBranchId = data.branchId;
    this.loadTargetForBranch();
  }

  onPeriodChange(data: {year: number, month: number}): void {
    this.selectedYear = data.year;
    this.selectedMonth = data.month;
    if (this.selectedBranchId) {
      this.loadTargetForBranch();
    }
  }

  onSubmitTarget(data: {branchId: number, year: number, month: number, amount: number}): void {
    const targetRequest: TargetSaveRequest = {
      target: data.amount,
      targetYear: data.year,
      targetMonth: data.month,
      branchId: data.branchId
    };

    this.targetService.createTarget(targetRequest).subscribe({
      next: (response) => {
        this.showMessage('Target set successfully!', 'success');
        this.loadTargetForBranch(); // Refresh the target display
      },
      error: (error) => {
        console.error('Error setting target:', error);
        this.showMessage('Failed to set target. Please try again.', 'error');
      }
    });
  }

  async onUpdateTarget(data: {id: number, amount: number}): Promise<void> {
    if (!this.existingTarget) {
      this.showMessage('No target found to update', 'error');
      return;
    }

    const updateRequest = {
      target: data.amount,
      targetYear: this.selectedYear,
      targetMonth: this.selectedMonth,
      branchId: this.selectedBranchId
    };

    this.targetService.updateTarget(data.id, updateRequest).subscribe({
      next: (response) => {
        this.showMessage('Target updated successfully!', 'success');
        this.loadTargetForBranch(); // Refresh the target display
      },
      error: (error) => {
        console.error('Error updating target:', error);
        this.showMessage('Failed to update target. Please try again.', 'error');
      }
    });
  }

  async onUploadExcel(data: {year: number, month: number, file: File}): Promise<void> {
    // First check if there are existing targets for this period
    try {
      const existingCheck = await this.targetService.checkExistingTargets(data.year, data.month).toPromise();
      
      if (existingCheck && existingCheck.hasExistingTargets) {
        // Show confirmation dialog if targets already exist
        const confirmResult = await Swal.fire({
          title: '<h3 style="color: #ffc107;">⚠️ Existing Targets Found</h3>',
          html: `
            <div style="text-align: center;">
              <p>There are already existing targets for <strong>${this.getMonthName(data.month)} ${data.year}</strong></p>
              <div style="background: #fff3cd; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #ffc107;">
                <strong>📊 Existing Targets:</strong> ${existingCheck.count || 0} target(s) found
              </div>
              <p style="color: #856404;">
                Uploading this Excel file will <strong>overwrite</strong> the existing targets.
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
      console.log('No existing targets check endpoint or error:', error);
      // Continue with upload if check fails
    }

    this.uploadExcelFile(data.year, data.month, data.file);
  }

  private async uploadExcelFile(year: number, month: number, file: File): Promise<void> {
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
          <p style="color: #6c757d; font-size: 0.9rem;">Please wait while we import your targets...</p>
        </div>
      `,
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    try {
      const response = await this.targetService.uploadExcelTargets(year, month, file).toPromise();
      
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
              ${response || 'Targets uploaded successfully!'}
            </p>
          </div>
        `,
        confirmButtonColor: '#28a745',
        timer: 4000,
        timerProgressBar: true
      });

      // Refresh data if we're viewing the same period
      if (this.selectedYear === year && this.selectedMonth === month && this.selectedBranchId) {
        this.loadTargetForBranch();
      }

    } catch (error: any) {
      console.error('Error uploading Excel file:', error);
      
      await Swal.fire({
        icon: 'error',
        title: 'Upload Failed',
        html: `
          <div style="text-align: center;">
            <p>Failed to upload Excel file</p>
            <div style="background: #fff2f2; padding: 15px; border-radius: 8px; margin: 15px 0; border-left: 4px solid #dc3545;">
              <strong>Error:</strong> ${error.error || error.message || 'Unknown error occurred'}
            </div>
            <p style="color: #6c757d; font-size: 0.9rem;">
              Please check your file format and try again.
            </p>
          </div>
        `,
        confirmButtonColor: '#dc3545'
      });
    }
  }

  private getMonthName(month: number): string {
    const months = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    return months[month - 1] || '';
  }

  private showMessage(message: string, type: 'success' | 'error'): void {
    Swal.fire({
      title: type === 'success' ? 'Success!' : 'Error!',
      text: message,
      icon: type,
      confirmButtonColor: type === 'success' ? '#28a745' : '#dc3545',
      timer: 3000,
      timerProgressBar: true,
      showConfirmButton: false
    });
  }
}
