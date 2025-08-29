import { Routes } from '@angular/router';

import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AdminUsersComponent } from './admin/users/users.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'admin/users', component: AdminUsersComponent, canActivate: [AuthGuard] },
  { 
    path: 'targets', 
    loadComponent: () => import('./components/target-management/target-management.component').then(m => m.TargetManagementComponent), 
    canActivate: [AuthGuard] 
  },
  { 
    path: 'collections', 
    loadComponent: () => import('./components/collection-management/collection-management.component').then(m => m.CollectionManagementComponent), 
    canActivate: [AuthGuard] 
  },
  { 
    path: 'analytics', 
    loadComponent: () => import('./components/analytics/analytics.component').then(m => m.AnalyticsComponent), 
    canActivate: [AuthGuard] 
  },
  { 
    path: 'admin/regions', 
    loadComponent: () => import('./components/region-management/region-management.component').then(m => m.RegionManagementComponent), 
    canActivate: [AuthGuard] 
  },
  { 
    path: 'admin/branches/:regionId', 
    loadComponent: () => import('./components/branch-management/branch-management.component').then(m => m.BranchManagementComponent), 
    canActivate: [AuthGuard] 
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];