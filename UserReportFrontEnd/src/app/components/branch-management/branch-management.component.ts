import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { BranchService, BranchResponse } from '../../services/branch.service';
import { RegionService } from '../../services/region.service';
import { CommonModule } from '@angular/common';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-branch-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './branch-management.component.html',
  styleUrls: ['./branch-management.component.css']
})
export class BranchManagementComponent implements OnInit {
  
  branches: BranchResponse[] = [];
  regionId: number = 0;
  regionName: string = '';
  loading = true;

  constructor(
    private authService: AuthService,
    private branchService: BranchService,
    private regionService: RegionService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check if user is admin
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/dashboard']);
      return;
    }

    // Get region ID from route
    this.route.params.subscribe(params => {
      this.regionId = +params['regionId'];
      this.loadBranches();
    });
  }

  async loadBranches(): Promise<void> {
    try {
      this.loading = true;
      
      // Get region name first
      if (!this.regionName) {
        const region = await this.regionService.getRegionById(this.regionId).toPromise();
        if (region) {
          this.regionName = region.rgnName;
        }
      }
      
      const response = await this.branchService.getBranchesByRegion(this.regionId).toPromise();
      this.branches = response || [];
      
      // Get region name from first branch if available (fallback)
      if (this.branches.length > 0 && !this.regionName) {
        this.regionName = this.branches[0].region.rgnName;
      }
    } catch (error) {
      console.error('Error loading branches:', error);
      await Swal.fire({
        icon: 'error',
        title: 'Error Loading Branches',
        text: 'Failed to load branches. Please try again.',
        confirmButtonColor: '#3498db'
      });
    } finally {
      this.loading = false;
    }
  }

  async addBranch(): Promise<void> {
    // Ensure we have the region name
    if (!this.regionName) {
      await Swal.fire({
        icon: 'error',
        title: 'Region Information Missing',
        text: 'Unable to determine region name. Please refresh the page.',
        confirmButtonColor: '#3498db'
      });
      return;
    }

    const { value: formValues } = await Swal.fire({
      title: '<h3 style="color: #2c3e50;">Add New Branch</h3>',
      html: `
        <div style="text-align: left; margin: 1rem 0;">
          <label style="display: block; margin-bottom: 0.5rem; color: #2c3e50; font-weight: 500;">Branch Name:</label>
          <input id="branchName" class="swal2-input" placeholder="Enter branch name" style="margin: 0;">
        </div>
        <div style="text-align: left; margin: 1rem 0;">
          <label style="display: block; margin-bottom: 0.5rem; color: #2c3e50; font-weight: 500;">Branch Description:</label>
          <textarea id="branchDescription" class="swal2-input" placeholder="Enter branch description" style="margin: 0; height: 80px;"></textarea>
        </div>
      `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: '<i class="fas fa-plus"></i> Add Branch',
      cancelButtonText: '<i class="fas fa-times"></i> Cancel',
      confirmButtonColor: '#28a745',
      cancelButtonColor: '#dc3545',
      preConfirm: () => {
        const branchName = (document.getElementById('branchName') as HTMLInputElement).value;
        const branchDescription = (document.getElementById('branchDescription') as HTMLTextAreaElement).value;

        if (!branchName || !branchDescription) {
          Swal.showValidationMessage('Please fill in all required fields');
          return false;
        }

        return {
          brnName: branchName,
          brnDes: branchDescription,
          region: this.regionName
        };
      }
    });

    if (formValues) {
      try {
        await this.branchService.createBranch(formValues).toPromise();
        
        await Swal.fire({
          icon: 'success',
          title: 'Branch Added Successfully!',
          text: `Branch "${formValues.brnName}" has been added successfully.`,
          confirmButtonColor: '#28a745',
          timer: 2000,
          timerProgressBar: true
        });

        this.loadBranches(); // Refresh the list
      } catch (error) {
        console.error('Error adding branch:', error);
        await Swal.fire({
          icon: 'error',
          title: 'Error Adding Branch',
          text: 'Failed to add branch. Please try again.',
          confirmButtonColor: '#dc3545'
        });
      }
    }
  }

  async editBranch(branch: BranchResponse): Promise<void> {
    const formValues = await Swal.fire({
      title: '<h3 style="color: #007bff;">Edit Branch</h3>',
      html: `
        <div style="text-align: left; margin: 1rem 0;">
          <label style="display: block; margin-bottom: 0.5rem; color: #2c3e50; font-weight: 500;">Branch Name:</label>
          <input id="editBranchName" class="swal2-input" placeholder="Enter branch name" value="${branch.brnName}" style="margin: 0;">
        </div>
        <div style="text-align: left; margin: 1rem 0;">
          <label style="display: block; margin-bottom: 0.5rem; color: #2c3e50; font-weight: 500;">Branch Description:</label>
          <textarea id="editBranchDescription" class="swal2-input" placeholder="Enter branch description" style="margin: 0; height: 80px;">${branch.brnDes}</textarea>
        </div>
      `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: '<i class="fas fa-save"></i> Update Branch',
      cancelButtonText: '<i class="fas fa-times"></i> Cancel',
      confirmButtonColor: '#007bff',
      cancelButtonColor: '#6c757d',
      preConfirm: () => {
        const branchName = (document.getElementById('editBranchName') as HTMLInputElement).value;
        const branchDescription = (document.getElementById('editBranchDescription') as HTMLTextAreaElement).value;

        if (!branchName || !branchDescription) {
          Swal.showValidationMessage('Please fill in all required fields');
          return false;
        }

        return {
          brnName: branchName,
          brnDes: branchDescription,
          region: this.regionName
        };
      }
    });

    if (formValues.isConfirmed && formValues.value) {
      try {
        await this.branchService.updateBranch(branch.id, formValues.value).toPromise();
        
        await Swal.fire({
          icon: 'success',
          title: 'Branch Updated Successfully!',
          text: `Branch "${formValues.value.brnName}" has been updated successfully.`,
          confirmButtonColor: '#28a745',
          timer: 2000,
          timerProgressBar: true
        });

        this.loadBranches(); // Refresh the list
      } catch (error) {
        console.error('Error updating branch:', error);
        await Swal.fire({
          icon: 'error',
          title: 'Error Updating Branch',
          text: 'Failed to update branch. Please try again.',
          confirmButtonColor: '#dc3545'
        });
      }
    }
  }

  async deleteBranch(branch: BranchResponse): Promise<void> {
    const result = await Swal.fire({
      title: '<h3 style="color: #dc3545;">Delete Branch</h3>',
      html: `
        <p style="margin: 1rem 0; color: #6c757d;">Are you sure you want to delete this branch?</p>
        <div style="background: #f8f9fa; padding: 1rem; border-radius: 8px; margin: 1rem 0;">
          <strong style="color: #2c3e50;">Branch Name:</strong> ${branch.brnName}<br>
          <strong style="color: #2c3e50;">Description:</strong> ${branch.brnDes}<br>
          <strong style="color: #2c3e50;">Region:</strong> ${branch.region.rgnName}
        </div>
        <p style="color: #dc3545; font-weight: 500; margin-top: 1rem;">
          <i class="fas fa-exclamation-triangle"></i> This action cannot be undone!
        </p>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: '<i class="fas fa-trash"></i> Delete Branch',
      cancelButtonText: '<i class="fas fa-times"></i> Cancel',
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d'
    });

    if (result.isConfirmed) {
      try {
        await this.branchService.deleteBranch(branch.id).toPromise();
        
        await Swal.fire({
          icon: 'success',
          title: 'Branch Deleted Successfully!',
          text: `Branch "${branch.brnName}" has been deleted.`,
          confirmButtonColor: '#28a745',
          timer: 2000,
          timerProgressBar: true
        });

        this.loadBranches(); // Refresh the list
      } catch (error) {
        console.error('Error deleting branch:', error);
        await Swal.fire({
          icon: 'error',
          title: 'Error Deleting Branch',
          text: 'Failed to delete branch. Please try again.',
          confirmButtonColor: '#dc3545'
        });
      }
    }
  }

  goBackToRegions(): void {
    this.router.navigate(['/admin/regions']);
  }
}
