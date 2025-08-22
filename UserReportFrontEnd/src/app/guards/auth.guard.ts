import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const AuthGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // 1. User is logged in?
  if (!auth.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  // 2. User is trying to access a protected route?
  if (state.url.startsWith('/admin')) {
    if (auth.getRole() !== 'ADMIN') {
      alert('You do not have admin access.');
      router.navigate(['/dashboard']);
      return false;
    }
  }

  return true;
};