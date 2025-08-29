import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RegionService, Region } from '../../services/region.service';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-region-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './region-management.component.html',
  styleUrl: './region-management.component.css'
})
export class RegionManagementComponent implements OnInit {
  regions: Region[] = [];
  isAdmin: boolean = false;
  loading: boolean = false;

  constructor(
    private regionService: RegionService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    if (!this.isAdmin) {
      Swal.fire({
        title: 'Access Denied',
        text: 'You do not have permission to access region management.',
        icon: 'error'
      });
      this.router.navigate(['/dashboard']);
      return;
    }
    this.loadRegions();
  }

  loadRegions(): void {
    this.loading = true;
    this.regionService.getAllRegions().subscribe({
      next: (regions) => {
        this.regions = regions;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading regions:', error);
        Swal.fire({
          title: 'Error!',
          text: 'Failed to load regions.',
          icon: 'error'
        });
        this.loading = false;
      }
    });
  }

  addRegion(): void {
    Swal.fire({
      title: 'Add New Region',
      html: `
        <input id="regionName" class="swal2-input" placeholder="Region Name" required>
        <textarea id="regionDescription" class="swal2-textarea" placeholder="Region Description"></textarea>
      `,
      showCancelButton: true,
      confirmButtonText: 'Add Region',
      confirmButtonColor: '#28a745',
      cancelButtonColor: '#6c757d',
      preConfirm: () => {
        const name = (document.getElementById('regionName') as HTMLInputElement).value;
        const description = (document.getElementById('regionDescription') as HTMLTextAreaElement).value;
        
        if (!name.trim()) {
          Swal.showValidationMessage('Region name is required');
          return false;
        }
        
        return { name: name.trim(), description: description.trim() };
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.createRegion(result.value);
      }
    });
  }

  createRegion(regionData: { name: string, description: string }): void {
    // Convert frontend field names to backend field names
    const backendData = {
      rgnName: regionData.name,
      rgnDes: regionData.description
    };
    
    this.regionService.createRegion(backendData).subscribe({
      next: () => {
        Swal.fire({
          title: 'Success!',
          text: 'Region created successfully!',
          icon: 'success',
          timer: 3000,
          showConfirmButton: false
        });
        this.loadRegions();
      },
      error: (error) => {
        console.error('Error creating region:', error);
        Swal.fire({
          title: 'Error!',
          text: 'Failed to create region.',
          icon: 'error'
        });
      }
    });
  }

  editRegion(region: Region): void {
    Swal.fire({
      title: '<h3 style="color: #3498db;">Edit Region</h3>',
      html: `
        <div style="text-align: left; margin: 1rem 0;">
          <label style="display: block; margin-bottom: 0.5rem; color: #2c3e50; font-weight: 500;">Region Name:</label>
          <input id="regionName" class="swal2-input" value="${region.rgnName}" style="margin: 0;">
        </div>
        <div style="text-align: left; margin: 1rem 0;">
          <label style="display: block; margin-bottom: 0.5rem; color: #2c3e50; font-weight: 500;">Region Description:</label>
          <textarea id="regionDescription" class="swal2-input" style="margin: 0; height: 80px;">${region.rgnDes || ''}</textarea>
        </div>
      `,
      focusConfirm: false,
      showCancelButton: true,
      confirmButtonText: '<i class="fas fa-save"></i> Update Region',
      cancelButtonText: '<i class="fas fa-times"></i> Cancel',
      confirmButtonColor: '#3498db',
      cancelButtonColor: '#6c757d',
      preConfirm: () => {
        const name = (document.getElementById('regionName') as HTMLInputElement).value;
        const description = (document.getElementById('regionDescription') as HTMLTextAreaElement).value;
        
        if (!name.trim()) {
          Swal.showValidationMessage('Region name is required');
          return false;
        }
        
        return { name: name.trim(), description: description.trim() };
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.updateRegion(region.id, result.value);
      }
    });
  }

  updateRegion(regionId: number, regionData: { name: string, description: string }): void {
    // Convert frontend field names to backend field names
    const backendData = {
      rgnName: regionData.name,
      rgnDes: regionData.description
    };
    
    this.regionService.updateRegion(regionId, backendData).subscribe({
      next: () => {
        Swal.fire({
          title: 'Success!',
          text: 'Region updated successfully!',
          icon: 'success',
          timer: 3000,
          showConfirmButton: false
        });
        this.loadRegions();
      },
      error: (error) => {
        console.error('Error updating region:', error);
        Swal.fire({
          title: 'Error!',
          text: 'Failed to update region.',
          icon: 'error'
        });
      }
    });
  }

  deleteRegion(region: Region): void {
    Swal.fire({
      title: 'Delete Region',
      html: `
        <div style="text-align: left; margin: 10px 0;">
          <strong>Region:</strong> ${region.rgnName}<br>
          <strong>Description:</strong> ${region.rgnDes || 'No description'}
        </div>
        <p style="color: #dc3545;">This action cannot be undone!</p>
        <p>Are you sure you want to delete this region?</p>
      `,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, Delete',
      cancelButtonText: 'Cancel',
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d'
    }).then((result) => {
      if (result.isConfirmed) {
        this.performDeleteRegion(region.id);
      }
    });
  }

  performDeleteRegion(regionId: number): void {
    this.regionService.deleteRegion(regionId).subscribe({
      next: () => {
        Swal.fire({
          title: 'Deleted!',
          text: 'Region has been deleted successfully.',
          icon: 'success',
          timer: 3000,
          showConfirmButton: false
        });
        this.loadRegions();
      },
      error: (error) => {
        console.error('Error deleting region:', error);
        Swal.fire({
          title: 'Error!',
          text: 'Failed to delete region. It may have associated branches.',
          icon: 'error'
        });
      }
    });
  }

  viewBranches(regionId: number): void {
    this.router.navigate(['/admin/branches', regionId]);
  }
}
