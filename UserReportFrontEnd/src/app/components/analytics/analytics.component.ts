import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChartComponent, ChartDataset } from '../shared/chart/chart.component';
import { RegionService, Region } from '../../services/region.service';
import { BranchService, BranchResponse } from '../../services/branch.service';
import { TargetService } from '../../services/target.service';
import { CollectionService } from '../../services/collection.service';
import { AuthService } from '../../services/auth.service';

interface CollectionResponseDTO {
  id: number;
  target: number;
  due: number;
  collectionAmount: number;
  percentage: number;
  collectionYear: number;
  collectionMonth: number;
  branchName: string;
  regionName: string;
  createdDatetime: string;
  modifyDatetime: string;
  createdByUsername: string;
  modifyByUsername: string;
}

interface MonthlyData {
  month: string;
  monthNumber: number;
  target: number;
  collection: number;
  achievement: number;
}

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, ChartComponent],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.css'
})
export class AnalyticsComponent implements OnInit {
  regions: Region[] = [];
  branches: BranchResponse[] = [];
  
  selectedRegionId: number | null = null;
  selectedBranchId: number | null = null;
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number | string | null = null;
  
  monthlyData: MonthlyData[] = [];
  loading = false;
  dataLoaded = false;
  
  viewMode: 'regional' | 'branch' = 'regional';
  
  // Chart properties
  chartLabels: string[] = [];
  chartDatasets: ChartDataset[] = [];
  showChart: boolean = false;
  
  years: number[] = [];
  months = [
    { value: null, name: 'All Months' },
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

  totalTarget: number = 0;
  totalCollection: number = 0;
  overallAchievement: number = 0;

  // User role management
  isBranchUser: boolean = false;
  userBranchId: number | null = null;
  userRegionId: number | null = null;

  constructor(
    private regionService: RegionService,
    private branchService: BranchService,
    private targetService: TargetService,
    private collectionService: CollectionService,
    private authService: AuthService
  ) {
    const currentYear = new Date().getFullYear();
    for (let i = currentYear - 5; i <= currentYear + 5; i++) {
      this.years.push(i);
    }
  }

  ngOnInit(): void {
    this.checkUserRole();
    this.loadRegions();
  }

  checkUserRole(): void {
    this.isBranchUser = !this.authService.isAdminUser();
    
    if (this.isBranchUser) {
      this.userBranchId = this.authService.getUserBranchId();
      
      // Set view mode to branch analysis for branch users
      this.viewMode = 'branch';
      this.selectedBranchId = this.userBranchId;
      
      // Get region ID from the branch (we'll need to find it from loaded branches)
      // This will be set after branches are loaded
    }
  }

  loadRegions(): void {
    this.regionService.getAllRegions().subscribe({
      next: (regions) => {
        this.regions = regions;
        
        // If branch user, find their region and load branches
        if (this.isBranchUser && this.userBranchId) {
          this.findUserRegionAndLoadBranches();
        }
      },
      error: (error) => {
        console.error('Error loading regions:', error);
      }
    });
  }

  findUserRegionAndLoadBranches(): void {
    // We need to check each region to find which one contains the user's branch
    for (const region of this.regions) {
      this.branchService.getBranchesByRegion(region.id).subscribe({
        next: (branches) => {
          const userBranch = branches.find(b => b.id === this.userBranchId);
          if (userBranch) {
            this.userRegionId = region.id;
            this.selectedRegionId = region.id;
            this.selectedBranchId = this.userBranchId;
            this.branches = branches;
          }
        },
        error: (error) => {
          console.error('Error loading branches for region:', error);
        }
      });
    }
  }

  loadBranches(): void {
    if (this.selectedRegionId) {
      this.branchService.getBranchesByRegion(this.selectedRegionId).subscribe({
        next: (branches) => {
          this.branches = branches;
          
          if (this.isBranchUser) {
            // For branch users, keep their branch selected and set the region ID
            const userBranch = branches.find(b => b.id === this.userBranchId);
            if (userBranch) {
              this.userRegionId = this.selectedRegionId;
              this.selectedBranchId = this.userBranchId;
            }
          } else {
            this.selectedBranchId = null;
          }
        },
        error: (error) => {
          console.error('Error loading branches:', error);
        }
      });
    }
  }

  onViewModeChange(): void {
    this.selectedRegionId = null;
    this.selectedBranchId = null;
    this.branches = [];
    this.resetAnalyticsData();
  }

  onRegionChange(): void {
    if (this.viewMode === 'branch') {
      this.loadBranches();
    }
    this.loadAnalyticsData();
  }

  onFiltersChange(): void {
    // Always reset chart and data state when filters change
    this.clearChartData();
    this.resetAnalyticsData();
    this.loadAnalyticsData();
  }

  loadRegionalAnalysis(): void {
    this.loadAnalysisData('regional');
  }

  loadBranchAnalysis(): void {
    this.loadAnalysisData('branch');
  }

  loadAnalysisData(type: 'regional' | 'branch'): void {
    // Check if we have a valid month (not null, not 'null' string, and not empty)
    const hasValidMonth = this.selectedMonth && 
                         this.selectedMonth !== 'null' && 
                         this.selectedMonth !== null && 
                         this.selectedMonth !== '';
    
    if (hasValidMonth) {
      // Load data for specific month
      this.loadMonthlyData(type);
    } else {
      // Load data for entire year (All Months)
      this.loadYearlyData(type);
    }
  }

  loadAnalyticsData(): void {
    if (this.viewMode === 'regional' && !this.selectedRegionId) {
      this.resetAnalyticsData();
      return;
    }
    
    if (this.viewMode === 'branch' && (!this.selectedRegionId || !this.selectedBranchId)) {
      this.resetAnalyticsData();
      return;
    }

    this.loading = true;
    this.dataLoaded = false;
    
    if (this.viewMode === 'regional') {
      this.loadRegionalAnalysis();
    } else {
      this.loadBranchAnalysis();
    }
  }

  loadMonthlyData(type: 'regional' | 'branch'): void {
    const monthValue = typeof this.selectedMonth === 'string' ? 
                      parseInt(this.selectedMonth) : 
                      this.selectedMonth!;
    
    const observable = type === 'regional' 
      ? this.collectionService.getCollectionsByRegionYearMonth(this.selectedRegionId!, this.selectedYear, monthValue)
      : this.collectionService.getBranchCollectionsByYearMonth(this.selectedBranchId!, this.selectedYear, monthValue);
      
    observable.subscribe({
      next: (response: any) => {
        console.log('Monthly data response:', response); // Debug log
        this.processMonthlyData(response);
      },
      error: (error) => {
        console.error(`Error loading ${type} monthly data:`, error);
        this.loading = false;
        this.resetAnalyticsData();
      }
    });
  }

  loadYearlyData(type: 'regional' | 'branch'): void {
    const observable = type === 'regional'
      ? this.collectionService.getCollectionsByRegionYear(this.selectedRegionId!, this.selectedYear)
      : this.collectionService.getBranchCollectionsByYear(this.selectedBranchId!, this.selectedYear);
      
    observable.subscribe({
      next: (response: any) => {
        console.log('Yearly data response:', response); // Debug log
        this.processYearlyData(response);
      },
      error: (error) => {
        console.error(`Error loading ${type} yearly data:`, error);
        this.loading = false;
        this.resetAnalyticsData();
      }
    });
  }

  processMonthlyData(collections: CollectionResponseDTO[] | CollectionResponseDTO | any): void {
    // Ensure collections is an array
    let collectionsArray: CollectionResponseDTO[] = [];
    
    if (Array.isArray(collections)) {
      collectionsArray = collections;
    } else if (collections && typeof collections === 'object') {
      // If it's a single object, wrap it in an array
      collectionsArray = [collections];
    } else {
      console.error('Invalid collections data:', collections);
      this.resetAnalyticsData();
      return;
    }
    
    // Calculate totals for the specific month
    this.totalTarget = collectionsArray.reduce((sum, item) => sum + (item.target || 0), 0);
    this.totalCollection = collectionsArray.reduce((sum, item) => sum + (item.collectionAmount || 0), 0);
    this.overallAchievement = this.totalTarget > 0 ? Math.round((this.totalCollection / this.totalTarget) * 100) : 0;
    
    // For specific month: ensure NO chart is displayed
    this.clearChartData();
    this.dataLoaded = true;
    this.loading = false;
  }

  processYearlyData(collections: CollectionResponseDTO[] | CollectionResponseDTO | any): void {
    // Ensure collections is an array
    let collectionsArray: CollectionResponseDTO[] = [];
    
    if (Array.isArray(collections)) {
      collectionsArray = collections;
    } else if (collections && typeof collections === 'object') {
      // If it's a single object, wrap it in an array
      collectionsArray = [collections];
    } else {
      console.error('Invalid collections data:', collections);
      this.resetAnalyticsData();
      return;
    }
    
    // Calculate yearly totals
    this.totalTarget = collectionsArray.reduce((sum, item) => sum + (item.target || 0), 0);
    this.totalCollection = collectionsArray.reduce((sum, item) => sum + (item.collectionAmount || 0), 0);
    this.overallAchievement = this.totalTarget > 0 ? Math.round((this.totalCollection / this.totalTarget) * 100) : 0;
    
    // Generate monthly chart data for yearly view
    this.generateMonthlyChartData(collectionsArray);
    this.dataLoaded = true;
    this.loading = false;
  }

  private clearChartData(): void {
    this.monthlyData = [];
    this.chartLabels = [];
    this.chartDatasets = [];
    this.showChart = false;
    
    // Force change detection for chart visibility
    setTimeout(() => {
      this.showChart = false;
    }, 0);
  }

  generateMonthlyChartData(collections: CollectionResponseDTO[]): void {
    // Double-check: Only proceed if this is for All Months (yearly data)
    const isSpecificMonth = this.selectedMonth && 
                          this.selectedMonth !== 'null' && 
                          this.selectedMonth !== null && 
                          this.selectedMonth !== '';
                          
    if (isSpecificMonth) {
      this.clearChartData();
      return;
    }
    
    const monthlyMap = new Map<number, {target: number, collection: number}>();
    
    // Initialize all months with zero values
    for (let month = 1; month <= 12; month++) {
      monthlyMap.set(month, {target: 0, collection: 0});
    }
    
    // Aggregate data by month
    collections.forEach(item => {
      const monthData = monthlyMap.get(item.collectionMonth) || {target: 0, collection: 0};
      monthData.target += item.target || 0;
      monthData.collection += item.collectionAmount || 0;
      monthlyMap.set(item.collectionMonth, monthData);
    });
    
    // Generate monthly data array
    this.monthlyData = [];
    for (let month = 1; month <= 12; month++) {
      const data = monthlyMap.get(month)!;
      const achievement = data.target > 0 ? Math.round((data.collection / data.target) * 100) : 0;
      
      this.monthlyData.push({
        month: this.months.find(m => m.value === month)?.name || '',
        monthNumber: month,
        target: data.target,
        collection: data.collection,
        achievement: achievement
      });
    }

    // Update chart data - will only show chart for All Months
    this.updateChartData();
  }

  private updateChartData(): void {
    // Check if this is for a specific month (never show chart for specific month)
    const isSpecificMonth = this.selectedMonth && 
                          this.selectedMonth !== null && 
                          this.selectedMonth !== 'null' && 
                          this.selectedMonth !== '';
    
    if (isSpecificMonth || this.monthlyData.length === 0) {
      this.showChart = false;
      this.chartLabels = [];
      this.chartDatasets = [];
      return;
    }
    
    // Only update chart data for yearly view (All Months)
    this.chartLabels = this.monthlyData.map(data => data.month);
    
    this.chartDatasets = [
      {
        label: 'Targets',
        data: this.monthlyData.map(data => data.target),
        borderColor: 'rgb(54, 162, 235)',
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        tension: 0.4
      },
      {
        label: 'Collections',
        data: this.monthlyData.map(data => data.collection),
        borderColor: 'rgb(255, 99, 132)',
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
        tension: 0.4
      }
    ];
    
    // Only show chart for All Months view
    this.showChart = true;
  }

  resetAnalyticsData(): void {
    this.totalTarget = 0;
    this.totalCollection = 0;
    this.overallAchievement = 0;
    this.clearChartData();
    this.loading = false;
    this.dataLoaded = false;
  }

  getAchievementStatus(percentage: number): string {
    if (percentage >= 100) return 'excellent';
    if (percentage >= 80) return 'good';
    if (percentage >= 60) return 'moderate';
    return 'poor';
  }

  getRegionName(): string {
    if (this.userRegionId) {
      const region = this.regions.find(r => r.id === this.userRegionId);
      return region ? region.rgnName : 'Unknown Region';
    }
    return 'Unknown Region';
  }

  getBranchName(): string {
    if (this.userBranchId) {
      const branch = this.branches.find(b => b.id === this.userBranchId);
      return branch ? branch.brnName : 'Unknown Branch';
    }
    return 'Unknown Branch';
  }
}
