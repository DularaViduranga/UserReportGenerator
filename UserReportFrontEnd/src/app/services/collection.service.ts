import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Collection {
  id: number;
  collection: number;
  collectionYear: number;
  collectionMonth: number;
  branchId: number;
}

export interface CollectionSaveRequest {
  collectionAmount: number;
  collectionYear: number;
  collectionMonth: number;
  branchId: number;
}

export interface MonthlyCollectionSummary {
  totalCollection: number;
  totalBranches: number;
  year: number;
  month: number;
}

export interface YearlyCollectionSummary {
  totalCollection: number;
  totalBranches: number;
  year: number;
  monthlyBreakdown: MonthlyCollectionSummary[];
}

@Injectable({
  providedIn: 'root'
})
export class CollectionService {
  private apiUrl = 'http://localhost:8080/api/v1/collections';

  constructor(private http: HttpClient) { }

  createCollection(collection: CollectionSaveRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/create`, collection);
  }

  getAllCollections(): Observable<Collection[]> {
    return this.http.get<Collection[]>(`${this.apiUrl}/all`);
  }

  getCollectionsByRegion(regionId: number): Observable<Collection[]> {
    return this.http.get<Collection[]>(`${this.apiUrl}/getCollectionsByRegionId/${regionId}`);
  }

  getCollectionByBranchAndYearMonth(branchId: number, year: number, month: number): Observable<Collection> {
    return this.http.get<Collection>(`${this.apiUrl}/branch/${branchId}/year/${year}/month/${month}`);
  }

  getCollectionsByYear(year: number): Observable<Collection[]> {
    return this.http.get<Collection[]>(`${this.apiUrl}/year/${year}`);
  }

  getCollectionsByYearAndMonth(year: number, month: number): Observable<Collection[]> {
    return this.http.get<Collection[]>(`${this.apiUrl}/year/${year}/month/${month}`);
  }

  updateCollection(id: number, collection: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/update/${id}`, collection);
  }

  deleteCollection(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete/${id}`);
  }
}
