import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ManagementContainerComponent } from '../../shared/components/management-container/management-container.component';
import { ManagementPageConfig, GridConfig, GridColumn, GridAction, LoadingState } from '../../shared/models';
import { RegionService, Region } from '../../services/region.service';
import { BranchService, BranchResponse } from '../../services/branch.service';
import { TargetService, TargetSaveRequest } from '../../services/target.service';
import { AuthService } from '../../services/auth.service';
import Swal from 'sweetalert2';

interface TargetDisplay {
  id?: number;
  branchName: string;
  regionName: string;
  year: number;
  month: number;
  monthName: string;
  targetAmount: number;
  actualAmount?: number;
  achievement?: number;
  status: 'Active' | 'Pending' | 'Completed';
}

@Component({
  selector: 'app-target-management-new',
  standalone: true,
  imports: [CommonModule, FormsModule, ManagementContainerComponent],
  templateUrl: './target-management-new.component.html',
  styleUrl: './target-management-new.component.css'
})
export class TargetManagementNewComponent implements OnInit {
  // Configuration
  pageConfig: ManagementPageConfig = {
    title: 'Target Management',
    subtitle: 'Manage monthly sales targets for branches',
    icon: 'target',
    entityName: 'Target',
    entityNamePlural: 'Targets',
    breadcrumbs: [
      { label: 'Dashboard', url: '/dashboard', icon: 'home' },
      { label: 'Target Management', icon: 'target' }
    ],
    actions: [
      {
        label: 'Import Targets',
        icon: 'upload',
        color: 'primary',
        action: () => this.openExcelImport()
      },
      {
        label: 'Update Targets',
        icon: 'sync',
        color: 'warning',
        action: () => this.openExcelUpdate()
      },
      {
        label: 'Export Data',
        icon: 'download',
        color: 'info',
        action: () => this.exportData()
      }
    ]
  };

  gridConfig: GridConfig = {
    columns: [
      {
        key: 'branchName',
        header: 'Branch',
        sortable: true,
        filterable: true,
        type: 'text'
      },
      {
        key: 'regionName',
        header: 'Region',
        sortable: true,
        filterable: true,
        type: 'text'
      },
      {
        key: 'year',
        header: 'Year',
        sortable: true,
        type: 'number'
      },
      {
        key: 'monthName',
        header: 'Month',
        sortable: false,
        type: 'text'
      },
      {
        key: 'targetAmount',
        header: 'Target Amount',
        sortable: true,
        type: 'number',
        formatter: (value: number) => this.formatCurrency(value)
      },
      {
        key: 'actualAmount',
        header: 'Actual Amount',
        sortable: true,
        type: 'number',
        formatter: (value: number) => value ? this.formatCurrency(value) : '-'
      },
      {
        key: 'achievement',
        header: 'Achievement %',
        sortable: true,
        type: 'number',
        formatter: (value: number) => value ? `${value.toFixed(1)}%` : '-'
      },
      {
        key: 'status',
        header: 'Status',
        sortable: true,
        type: 'text',
        template: 'badge'
      }
    ],
    actions: [
      {
        icon: 'eye',
        label: 'View',
        color: 'info',
        action: 'view'
      },
      {
        icon: 'edit',
        label: 'Edit',
        color: 'primary',
        action: 'edit'
      },
      {
        icon: 'trash',
        label: 'Delete',
        color: 'danger',
        action: 'delete'
      }
    ],
    pageSize: 10,
    sortable: true,
    filterable: true,
    selectable: true,
    exportable: true
  };

  // Data
  targets: TargetDisplay[] = [];
  regions: Region[] = [];
  branches: BranchResponse[] = [];
  
  // State
  loadingState: LoadingState = { isLoading: false };
  isAdmin: boolean = false;
  userBranchId: number | null = null;

  // Filters
  selectedRegionId: number | null = null;
  selectedBranchId: number | null = null;
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number | null = null;

  constructor(
    private regionService: RegionService,
    private branchService: BranchService,
    private targetService: TargetService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkUserRole();
    this.loadInitialData();
  }

  private checkUserRole(): void {
    const userRole = this.authService.getUserRole();
    this.isAdmin = userRole === 'ADMIN';
    
    if (!this.isAdmin) {
      this.userBranchId = this.authService.getUserBranchId();
      this.selectedBranchId = this.userBranchId;
      this.pageConfig.createPermission = 'BRANCH_USER';
    }
  }

  private async loadInitialData(): Promise<void> {
    this.loadingState = { isLoading: true, message: 'Loading data...' };
    
    try {
      if (this.isAdmin) {
        await Promise.all([
          this.loadRegions(),
          this.loadBranches()
        ]);
      } else {
        await this.loadBranches();
      }
      
      await this.loadTargets();
    } catch (error) {
      console.error('Error loading initial data:', error);
      this.showError('Failed to load data. Please try again.');
    } finally {
      this.loadingState = { isLoading: false };
    }
  }

  private async loadRegions(): Promise<void> {
    try {
      this.regions = await this.regionService.getAllRegions().toPromise() || [];
    } catch (error) {
      console.error('Error loading regions:', error);
    }
  }

  private async loadBranches(): Promise<void> {
    try {
      if (this.selectedRegionId) {
        this.branches = await this.branchService.getBranchesByRegion(this.selectedRegionId).toPromise() || [];
      } else {
        this.branches = await this.branchService.getAllBranches().toPromise() || [];
      }
    } catch (error) {
      console.error('Error loading branches:', error);
    }
  }

  private async loadTargets(): Promise<void> {
    try {
      // This would be replaced with actual API call
      // For now, we'll create some mock data
      this.targets = this.generateMockTargets();
    } catch (error) {
      console.error('Error loading targets:', error);
    }
  }

  // Event Handlers
  onCreateTarget(): void {
    // Navigate to create form or open modal
    this.router.navigate(['/target-management/create']);
  }

  onEditTarget(target: TargetDisplay): void {
    if (target.id) {
      this.router.navigate(['/target-management/edit', target.id]);
    }
  }

  onViewTarget(target: TargetDisplay): void {
    if (target.id) {
      this.router.navigate(['/target-management/view', target.id]);
    }
  }

  onDeleteTarget(target: TargetDisplay): void {
    Swal.fire({
      title: 'Are you sure?',
      text: `Do you want to delete the target for ${target.branchName} - ${target.monthName} ${target.year}?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Yes, delete it!'
    }).then((result) => {
      if (result.isConfirmed && target.id) {
        this.deleteTarget(target.id);
      }
    });
  }

  onSearchChange(searchTerm: string): void {
    // Implement search logic
    console.log('Search term:', searchTerm);
  }

  onFilterChange(filters: any): void {
    // Implement filter logic
    console.log('Filters changed:', filters);
    this.applyFilters();
  }

  onSortChange(sortOptions: { column: string; direction: 'asc' | 'desc' }): void {
    // Implement sorting logic
    console.log('Sort options:', sortOptions);
    this.sortTargets(sortOptions.column, sortOptions.direction);
  }

  onPageChange(pageInfo: { page: number; pageSize: number }): void {
    // Implement pagination logic
    console.log('Page info:', pageInfo);
  }

  onBulkAction(action: { action: string; items: TargetDisplay[] }): void {
    if (action.action === 'delete') {
      this.bulkDeleteTargets(action.items);
    }
  }

  // Filter Methods
  onRegionChange(): void {
    this.selectedBranchId = null;
    this.loadBranches().then(() => {
      this.applyFilters();
    });
  }

  onBranchChange(): void {
    this.applyFilters();
  }

  onYearChange(): void {
    this.applyFilters();
  }

  onMonthChange(): void {
    this.applyFilters();
  }

  private applyFilters(): void {
    // Apply filters to the target data
    // This would typically involve making an API call with filter parameters
    this.loadTargets();
  }

  clearFilters(): void {
    this.selectedRegionId = null;
    this.selectedBranchId = this.isAdmin ? null : this.userBranchId;
    this.selectedYear = new Date().getFullYear();
    this.selectedMonth = null;
    this.applyFilters();
  }

  // Action Methods
  openExcelImport(): void {
    // Open Excel import dialog/component
    console.log('Opening Excel import for targets...');
    alert('Excel Import functionality will be implemented here');
  }

  openExcelUpdate(): void {
    // Open Excel update dialog/component (Admin only)
    console.log('Opening Excel update for targets...');
    alert('Excel Update functionality will be implemented here');
  }

  exportData(): void {
    // Export current data
    console.log('Exporting target data...');
    alert('Export functionality will be implemented here');
  }

  private async deleteTarget(targetId: number): Promise<void> {
    try {
      this.loadingState = { isLoading: true, message: 'Deleting target...' };
      // await this.targetService.deleteTarget(targetId).toPromise();
      
      // Remove from local array for demo
      this.targets = this.targets.filter(t => t.id !== targetId);
      
      Swal.fire('Deleted!', 'Target has been deleted.', 'success');
    } catch (error) {
      console.error('Error deleting target:', error);
      this.showError('Failed to delete target. Please try again.');
    } finally {
      this.loadingState = { isLoading: false };
    }
  }

  private bulkDeleteTargets(targets: TargetDisplay[]): void {
    Swal.fire({
      title: 'Are you sure?',
      text: `Do you want to delete ${targets.length} selected target(s)?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Yes, delete them!'
    }).then((result) => {
      if (result.isConfirmed) {
        // Delete selected targets
        const targetIds = targets.filter(t => t.id).map(t => t.id!);
        targetIds.forEach(id => this.deleteTarget(id));
      }
    });
  }

  private sortTargets(column: string, direction: 'asc' | 'desc'): void {
    this.targets.sort((a, b) => {
      const aValue = (a as any)[column];
      const bValue = (b as any)[column];
      
      let comparison = 0;
      if (aValue > bValue) comparison = 1;
      if (aValue < bValue) comparison = -1;
      
      return direction === 'desc' ? comparison * -1 : comparison;
    });
  }

  // Utility Methods
  private formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  }

  private showError(message: string): void {
    Swal.fire('Error', message, 'error');
  }

  // Mock Data Generator (for demo purposes)
  private generateMockTargets(): TargetDisplay[] {
    const months = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];
    
    return [
      {
        id: 1,
        branchName: 'Main Branch',
        regionName: 'Central Region',
        year: 2024,
        month: 1,
        monthName: 'January',
        targetAmount: 100000,
        actualAmount: 95000,
        achievement: 95.0,
        status: 'Completed'
      },
      {
        id: 2,
        branchName: 'Downtown Branch',
        regionName: 'Central Region',
        year: 2024,
        month: 1,
        monthName: 'January',
        targetAmount: 80000,
        actualAmount: 85000,
        achievement: 106.3,
        status: 'Completed'
      },
      {
        id: 3,
        branchName: 'North Branch',
        regionName: 'Northern Region',
        year: 2024,
        month: 2,
        monthName: 'February',
        targetAmount: 120000,
        actualAmount: undefined,
        achievement: undefined,
        status: 'Active'
      }
    ];
  }

  get monthOptions() {
    return [
      { value: null, label: 'All Months' },
      { value: 1, label: 'January' },
      { value: 2, label: 'February' },
      { value: 3, label: 'March' },
      { value: 4, label: 'April' },
      { value: 5, label: 'May' },
      { value: 6, label: 'June' },
      { value: 7, label: 'July' },
      { value: 8, label: 'August' },
      { value: 9, label: 'September' },
      { value: 10, label: 'October' },
      { value: 11, label: 'November' },
      { value: 12, label: 'December' }
    ];
  }

  get yearOptions() {
    const currentYear = new Date().getFullYear();
    const years = [];
    for (let i = currentYear - 2; i <= currentYear + 1; i++) {
      years.push(i);
    }
    return years;
  }
}