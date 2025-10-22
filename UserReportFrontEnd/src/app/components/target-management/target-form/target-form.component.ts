import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-target-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './target-form.component.html',
  styleUrl: './target-form.component.css'
})
export class TargetFormComponent implements OnInit, OnChanges {
  @Input() isAdmin: boolean = false;
  @Input() regions: any[] = [];
  @Input() branches: any[] = [];
  @Input() currentTarget: any = null;
  @Input() userBranchId: number | null = null;
  @Input() userBranchName: string | null = null;
  @Input() selectedYear: number = new Date().getFullYear();
  @Input() selectedMonth: number = new Date().getMonth() + 1;
  @Input() loading: boolean = false;

  @Output() regionChange = new EventEmitter<number>();
  @Output() branchChange = new EventEmitter<{branchId: number, regionId: number}>();
  @Output() submitTarget = new EventEmitter<{branchId: number, year: number, month: number, amount: number}>();
  @Output() updateTarget = new EventEmitter<{id: number, amount: number}>();
  @Output() periodChange = new EventEmitter<{year: number, month: number}>();

  selectedRegionId: number | null = null;
  selectedBranchId: number | null = null;
  selectedBranch: string = '';
  selectedYearMonth: string = '';
  targetAmount: number = 0;
  isUpdateMode: boolean = false;

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

  ngOnInit(): void {
    this.initializeYears();
    this.initializeYearMonth();
    this.initializeBranchData();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentTarget'] && this.currentTarget) {
      this.isUpdateMode = true;
      this.targetAmount = this.currentTarget.target || 0;
    } else if (changes['currentTarget'] && !this.currentTarget) {
      this.isUpdateMode = false;
      this.targetAmount = 0;
    }
    
    // Update year/month display when they change from parent
    if (changes['selectedYear'] || changes['selectedMonth']) {
      this.initializeYearMonth();
    }
  }

  initializeYears(): void {
    const currentYear = new Date().getFullYear();
    this.years = [];
    for (let year = currentYear - 2; year <= currentYear + 3; year++) {
      this.years.push(year);
    }
  }

  initializeYearMonth(): void {
    const year = this.selectedYear.toString().padStart(4, '0');
    const month = this.selectedMonth.toString().padStart(2, '0');
    this.selectedYearMonth = `${year}-${month}`;
  }

  initializeBranchData(): void {
    if (!this.isAdmin && this.userBranchId && this.userBranchName) {
      this.selectedBranchId = this.userBranchId;
      this.selectedBranch = this.userBranchName;
      
      // Trigger target lookup for branch user
      this.branchChange.emit({
        branchId: this.selectedBranchId,
        regionId: 0 // For branch users, we don't need region ID
      });
    }
  }

  onRegionChange(): void {
    this.selectedBranchId = null;
    this.selectedBranch = '';
    this.currentTarget = null;
    this.isUpdateMode = false;
    this.targetAmount = 0;
    
    if (this.selectedRegionId) {
      this.regionChange.emit(this.selectedRegionId);
    }
  }

  onBranchChange(): void {
    if (this.selectedBranchId && this.selectedRegionId) {
      const branch = this.branches.find(b => b.id === Number(this.selectedBranchId));
      this.selectedBranch = branch ? branch.brnName : '';
      this.branchChange.emit({
        branchId: this.selectedBranchId,
        regionId: this.selectedRegionId
      });
    }
  }

  onYearMonthChange(): void {
    if (this.selectedYearMonth) {
      const [year, month] = this.selectedYearMonth.split('-');
      this.selectedYear = parseInt(year);
      this.selectedMonth = parseInt(month);
      
      // Emit period change to parent
      this.periodChange.emit({
        year: this.selectedYear,
        month: this.selectedMonth
      });
      
      this.checkForBranchChange();
    }
  }

  checkForBranchChange(): void {
    if (this.selectedBranchId) {
      if (this.isAdmin && this.selectedRegionId) {
        this.branchChange.emit({
          branchId: this.selectedBranchId,
          regionId: this.selectedRegionId
        });
      } else if (!this.isAdmin) {
        this.branchChange.emit({
          branchId: this.selectedBranchId,
          regionId: 0 // For branch users, we don't need region ID
        });
      }
    }
  }

  onSubmit(): void {
    if (!this.selectedBranchId || !this.targetAmount || this.targetAmount <= 0) {
      Swal.fire({
        title: 'Missing Information',
        text: 'Please enter a valid target amount',
        icon: 'warning',
        confirmButtonColor: '#f39c12'
      });
      return;
    }

    if (this.isUpdateMode && this.currentTarget) {
      this.updateTarget.emit({
        id: this.currentTarget.id,
        amount: this.targetAmount
      });
    } else {
      this.submitTarget.emit({
        branchId: this.selectedBranchId,
        year: this.selectedYear,
        month: this.selectedMonth,
        amount: this.targetAmount
      });
    }
  }

  resetForm(): void {
    this.targetAmount = 0;
    this.isUpdateMode = false;
    this.currentTarget = null;
  }

  // Getters for template
  get canSubmit(): boolean {
    return !!(this.selectedBranchId && this.targetAmount && this.targetAmount > 0 && 
             this.selectedYear && this.selectedMonth);
  }

  get submitButtonText(): string {
    return this.isUpdateMode ? 'Update Target' : 'Set Target';
  }

  get selectedMonthName(): string {
    const month = this.months.find(m => m.value === this.selectedMonth);
    return month ? month.name : '';
  }
}
