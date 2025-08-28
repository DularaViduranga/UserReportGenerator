import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Branch {
  id: number;
  brnName: string;
  brnDes: string;
  region: {
    id: number;
    rgnName: string;
  };
}

export interface BranchResponse {
  id: number;
  brnName: string;
  brnDes: string;
  region: {
    id: number;
    rgnName: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class BranchService {
  private apiUrl = 'http://localhost:8080/api/v1/branches';

  constructor(private http: HttpClient) { }

  getAllBranches(): Observable<Branch[]> {
    return this.http.get<Branch[]>(`${this.apiUrl}/all`);
  }

  getBranchesByRegion(regionId: number): Observable<BranchResponse[]> {
    return this.http.get<BranchResponse[]>(`${this.apiUrl}/branchResponsesByRegionId/${regionId}`);
  }

  createBranch(branch: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/create`, branch);
  }

  updateBranch(id: number, branch: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/update/${id}`, branch);
  }

  deleteBranch(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete/${id}`);
  }
}
