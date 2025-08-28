import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RegionService, Region } from '../../services/region.service';
import { BranchService, BranchResponse } from '../../services/branch.service';
import { TargetService } from '../../services/target.service';
import { CollectionService } from '../../services/collection.service';

interface ChartData {
  regionName: string;
  targetAmount: number;
  collectionAmount: number;
  achievementPercentage: number;
  branches: BranchData[];
}

interface BranchData {
  branchName: string;
  targetAmount: number;
  collectionAmount: number;
  achievementPercentage: number;
}

interface MonthlyData {
  month: string;
  target: number;
  collection: number;
  achievement: number;
}

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.css'
})
export class AnalyticsComponent implements OnInit {
  regions: Region[] = [];
  branches: BranchResponse[] = [];
  
  selectedRegionId: number | null = null;
  selectedYear: number = new Date().getFullYear();
  selectedMonth: number | null = null;
  
  chartData: ChartData[] = [];
  monthlyData: MonthlyData[] = [];
  loading = false;
  
  viewMode: 'regional' | 'monthly' = 'regional';
  
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

  constructor(
    private regionService: RegionService,
    private branchService: BranchService,
    private targetService: TargetService,
    private collectionService: CollectionService
  ) {
    // Generate years (current year - 5 to current year)
    const currentYear = new Date().getFullYear();
    for (let i = currentYear - 5; i <= currentYear; i++) {
      this.years.push(i);
    }
  }

  ngOnInit(): void {
    this.loadRegions();
    this.loadAnalyticsData();
  }

  loadRegions(): void {
    this.regionService.getAllRegions().subscribe({
      next: (regions) => {
        this.regions = regions;
      },
      error: (error) => {
        console.error('Error loading regions:', error);
      }
    });
  }

  onFiltersChange(): void {
    this.loadAnalyticsData();
  }

  loadAnalyticsData(): void {
    this.loading = true;
    
    if (this.viewMode === 'regional') {
      this.loadRegionalData();
    } else {
      this.loadMonthlyData();
    }
  }

  loadRegionalData(): void {
    // Load targets and collections data for regional view
    Promise.all([
      this.loadTargetsData(),
      this.loadCollectionsData()
    ]).then(() => {
      this.processRegionalData();
      this.loading = false;
    }).catch((error) => {
      console.error('Error loading analytics data:', error);
      this.loading = false;
    });
  }

  loadMonthlyData(): void {
    // Generate monthly data for the selected year
    const monthlyPromises = [];
    
    for (let month = 1; month <= 12; month++) {
      monthlyPromises.push(this.getMonthlyDataForMonth(month));
    }
    
    Promise.all(monthlyPromises).then((results) => {
      this.monthlyData = results.filter(data => data !== null) as MonthlyData[];
      this.loading = false;
    }).catch((error) => {
      console.error('Error loading monthly data:', error);
      this.loading = false;
    });
  }

  async getMonthlyDataForMonth(month: number): Promise<MonthlyData | null> {
    try {
      const monthName = this.months.find(m => m.value === month)?.name || '';
      
      // Get targets and collections for this month
      const [targets, collections] = await Promise.all([
        this.targetService.getTargetsByYearAndMonth(this.selectedYear, month).toPromise(),
        this.collectionService.getCollectionsByYearAndMonth(this.selectedYear, month).toPromise()
      ]);
      
      const totalTarget = targets?.reduce((sum, t) => sum + (t.target || 0), 0) || 0;
      const totalCollection = collections?.reduce((sum, c) => sum + (c.collection || 0), 0) || 0;
      const achievement = totalTarget > 0 ? (totalCollection / totalTarget) * 100 : 0;
      
      return {
        month: monthName,
        target: totalTarget,
        collection: totalCollection,
        achievement: Math.round(achievement)
      };
    } catch (error) {
      return null;
    }
  }

  async loadTargetsData(): Promise<any> {
    if (this.selectedMonth) {
      return this.targetService.getTargetsByYearAndMonth(this.selectedYear, this.selectedMonth).toPromise();
    } else {
      return this.targetService.getTargetsByYear(this.selectedYear).toPromise();
    }
  }

  async loadCollectionsData(): Promise<any> {
    if (this.selectedMonth) {
      return this.collectionService.getCollectionsByYearAndMonth(this.selectedYear, this.selectedMonth).toPromise();
    } else {
      return this.collectionService.getCollectionsByYear(this.selectedYear).toPromise();
    }
  }

  processRegionalData(): void {
    // Process and group data by regions
    // This is a simplified version - you would need to implement the actual data processing
    // based on your specific requirements and data structure
    
    this.chartData = [];
    this.totalTarget = 0;
    this.totalCollection = 0;
    
    // Calculate totals and overall achievement
    this.overallAchievement = this.totalTarget > 0 ? Math.round((this.totalCollection / this.totalTarget) * 100) : 0;
  }

  getAchievementStatus(percentage: number): string {
    if (percentage >= 100) return 'excellent';
    if (percentage >= 80) return 'good';
    if (percentage >= 60) return 'moderate';
    return 'poor';
  }

  getBarWidth(value: number, maxValue: number): number {
    return maxValue > 0 ? (value / maxValue) * 100 : 0;
  }

  getMaxValue(data: any[], field: string): number {
    return Math.max(...data.map(item => item[field] || 0));
  }
}
