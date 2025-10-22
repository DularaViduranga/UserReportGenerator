// src/app/dashboard/dashboard.component.ts

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  
  // Date properties
  currentDate = new Date();
  currentMonth = new Date();
  
  // Mock dashboard statistics - In real app, these would come from a service
  dashboardStats = {
    totalTargets: 245,
    totalCollections: 1850000,
    achievementRate: 78.5,
    activeBranches: 24,
    totalRegions: 5
  };

  constructor(public auth: AuthService, private router: Router) {}

  // Check if current user is admin
  get isAdmin(): boolean {
    return this.auth.getRole() === 'ADMIN';
  }

  // Logout method
  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}