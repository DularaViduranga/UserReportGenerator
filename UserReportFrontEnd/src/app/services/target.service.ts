import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Target {
  id: number;
  target: number;
  targetYear: number;
  targetMonth: number;
  branchId: number;
}

export interface TargetSaveRequest {
  target: number;
  targetYear: number;
  targetMonth: number;
  branchId: number;
}

export interface MonthlyTargetSummary {
  totalTarget: number;
  totalBranches: number;
  year: number;
  month: number;
}

export interface YearlyTargetSummary {
  totalTarget: number;
  totalBranches: number;
  year: number;
  monthlyBreakdown: MonthlyTargetSummary[];
}

@Injectable({
  providedIn: 'root'
})
export class TargetService {
  private apiUrl = 'http://localhost:8080/api/v1/targets';

  constructor(private http: HttpClient) { }

  createTarget(target: TargetSaveRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/create`, target);
  }

  getAllTargets(): Observable<Target[]> {
    return this.http.get<Target[]>(`${this.apiUrl}/all`);
  }

  getTargetsByRegion(regionId: number): Observable<Target[]> {
    return this.http.get<Target[]>(`${this.apiUrl}/region/${regionId}`);
  }

  getTargetsByBranchAndYearMonth(branchId: number, year: number, month: number): Observable<Target> {
    return this.http.get<Target>(`${this.apiUrl}/branch/${branchId}/year/${year}/month/${month}`);
  }

  getTargetsByYear(year: number): Observable<Target[]> {
    return this.http.get<Target[]>(`${this.apiUrl}/year/${year}`);
  }

  getTargetsByYearAndMonth(year: number, month: number): Observable<Target[]> {
    return this.http.get<Target[]>(`${this.apiUrl}/year/${year}/month/${month}`);
  }

  getMonthlyTargetSummary(year: number, month: number): Observable<MonthlyTargetSummary> {
    return this.http.get<MonthlyTargetSummary>(`${this.apiUrl}/summary/monthly/year/${year}/month/${month}`);
  }

  getMonthlyTargetSummaryByRegion(regionId: number, year: number, month: number): Observable<MonthlyTargetSummary> {
    return this.http.get<MonthlyTargetSummary>(`${this.apiUrl}/summary/monthly/region/${regionId}/year/${year}/month/${month}`);
  }

  getYearlyTargetSummary(year: number): Observable<YearlyTargetSummary> {
    return this.http.get<YearlyTargetSummary>(`${this.apiUrl}/summary/yearly/year/${year}`);
  }

  getYearlyTargetSummaryByRegion(regionId: number, year: number): Observable<YearlyTargetSummary> {
    return this.http.get<YearlyTargetSummary>(`${this.apiUrl}/summary/yearly/region/${regionId}/year/${year}`);
  }

  updateTarget(id: number, target: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/update/${id}`, target);
  }

  deleteTarget(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete/${id}`);
  }

  uploadExcelTargets(year: number, month: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.apiUrl}/upload/${year}/${month}`, formData, { 
      responseType: 'text' 
    });
  }

  checkExistingTargets(year: number, month: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/check-existing/${year}/${month}`);
  }
}
