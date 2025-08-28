import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Region {
  id: number;
  rgnName: string;
  rgnDes: string;
}

@Injectable({
  providedIn: 'root'
})
export class RegionService {
  private API_URL = 'http://localhost:8080/api/v1/regions';

  constructor(private http: HttpClient) {}

  getAllRegions(): Observable<Region[]> {
    return this.http.get<Region[]>(`${this.API_URL}/all`);
  }

  getRegionById(id: number): Observable<Region> {
    return this.http.get<Region>(`${this.API_URL}/response/${id}`);
  }

  createRegion(region: any): Observable<any> {
    return this.http.post<any>(`${this.API_URL}/create`, region);
  }

  updateRegion(id: number, region: any): Observable<any> {
    return this.http.put<any>(`${this.API_URL}/update/${id}`, region);
  }

  deleteRegion(id: number): Observable<any> {
    return this.http.delete<any>(`${this.API_URL}/delete/${id}`);
  }
}
