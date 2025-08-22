import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
  private API_URL = 'http://localhost:8080/api/v1/auth';

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

  deleteUser(userId: number): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.delete(`${this.API_URL}/admin/delete-user/${userId}`, { headers });
  }

  updateUserRole(userId: number, role: string): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.put(`${this.API_URL}/admin/update-role/${userId}?role=${role}`, {}, { headers });
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
    return !!localStorage.getItem('authToken');
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

  getToken(): string | null {
    if (typeof localStorage === 'undefined') return null;
    return localStorage.getItem('authToken');
  }

  logout(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('authToken');
    }
  }
}