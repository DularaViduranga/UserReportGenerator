import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'Co-op Life';
  
  // UI state management
  mobileMenuOpen = false;
  adminDropdownOpen = false;

  constructor(public auth: AuthService) {}

  toggleMobileSidebar(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMobileSidebar(): void {
    this.mobileMenuOpen = false;
  }

  toggleAdminDropdown(): void {
    this.adminDropdownOpen = !this.adminDropdownOpen;
  }
}
