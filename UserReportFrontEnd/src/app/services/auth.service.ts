import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environments';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  timestamp: string;
  error: string | null;
  message: string;
}

export interface RegisterResponse {
  message: string | null;
  error: string | null;
}

export interface User {
  id: number;
  name: string;
  email: string;
  username: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private API_URL = environment.apiUrl+'/api/v1/auth';

  constructor(private http: HttpClient) {}

  login(data: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API_URL}/login`, data);
  }

  register(data: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.API_URL}/register`, data);
  }

  getAllUsers(): Observable<User[]> {
    const headers = this.getAuthHeaders();
    console.log('Making request to:', `${this.API_URL}/admin/users`);
    console.log('Headers:', headers);
    return this.http.get<User[]>(`${this.API_URL}/admin/users`, { headers });
  }

  createAdmin(data: RegisterRequest): Observable<RegisterResponse> {
    const headers = this.getAuthHeaders();
    return this.http.post<RegisterResponse>(`${this.API_URL}/admin/create-admin`, data, { headers });
  }

  createBranchUser(data: any): Observable<RegisterResponse> {
    const headers = this.getAuthHeaders();
    return this.http.post<RegisterResponse>(`${this.API_URL}/admin/create-branch-user`, data, { headers });
  }

  deleteUser(userId: number): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.delete(`${this.API_URL}/admin/delete-user/${userId}`, { 
      headers,
      observe: 'response',
      responseType: 'text'
    });
  }

  updateUserRole(userId: number, role: string): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put(`${this.API_URL}/admin/update-role/${userId}?role=${role}`, {}, { 
      headers,
      observe: 'response',
      responseType: 'text'
    });
  }

  getCurrentUser(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get(`${this.API_URL}/current-user`, { headers });
  }

  private getAuthHeaders(): { [key: string]: string } {
    const token = this.getToken();
    if (token) {
      return { 'Authorization': `Bearer ${token}` };
    }
    return {};
  }

  isLoggedIn(): boolean {
    if (typeof localStorage === 'undefined') return false;
    const token = localStorage.getItem('authToken');
    if (!token) return false;
    
    // Check if token is expired
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      if (payload.exp && payload.exp < currentTime) {
        console.log('Token expired, removing from localStorage');
        this.logout();
        return false;
      }
      return true;
    } catch (error) {
      console.error('Error parsing JWT token:', error);
      this.logout();
      return false;
    }
  }

  getRole(): string {
    if (typeof localStorage === 'undefined') return '';
    const token = localStorage.getItem('authToken');
    if (!token) return '';
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      console.log('JWT Payload:', payload);
      return payload.role || '';
    } catch (error) {
      console.error('Error parsing JWT token:', error);
      return '';
    }
  }

  getUsername(): string {
    if (typeof localStorage === 'undefined') return '';
    const token = localStorage.getItem('authToken');
    if (!token) return '';
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub || payload.username || '';
    } catch (error) {
      console.error('Error parsing JWT token:', error);
      return '';
    }
  }

  getUserBranchId(): number | null {
    if (typeof localStorage === 'undefined') return null;
    const token = localStorage.getItem('authToken');
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.branchId || null;
    } catch (error) {
      console.error('Error parsing JWT token for branch ID:', error);
      return null;
    }
  }

  getUserBranchName(): string | null {
    if (typeof localStorage === 'undefined') return null;
    const token = localStorage.getItem('authToken');
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.branchName || null;
    } catch (error) {
      console.error('Error parsing JWT token for branch name:', error);
      return null;
    }
  }

  isAdminUser(): boolean {
    return this.getRole() === 'ADMIN';
  }

  isBranchUser(): boolean {
    return this.getRole() === 'USER' && this.getUserBranchId() !== null;
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  getToken(): string | null {
    if (typeof localStorage === 'undefined') {
      console.log('localStorage is undefined');
      return null;
    }
    const token = localStorage.getItem('authToken');
    console.log('Getting token from localStorage:', token ? 'Token exists' : 'No token found');
    return token;
  }

  // Debug method to check token details
  debugTokenInfo(): void {
    const token = this.getToken();
    console.log('=== AUTH DEBUG INFO ===');
    console.log('Token exists:', !!token);
    console.log('Token length:', token ? token.length : 0);
    
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        console.log('Token payload:', payload);
        console.log('Token expires at:', new Date(payload.exp * 1000));
        console.log('Current time:', new Date());
        console.log('Token expired:', payload.exp < Math.floor(Date.now() / 1000));
      } catch (error) {
        console.error('Error parsing token:', error);
      }
    }
    console.log('isLoggedIn():', this.isLoggedIn());
    console.log('========================');
  }

  logout(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('authToken');
    }
  }
}