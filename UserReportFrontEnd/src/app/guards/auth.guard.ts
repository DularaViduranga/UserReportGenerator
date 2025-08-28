import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import Swal from 'sweetalert2';

export const AuthGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  console.log('AuthGuard checking access to:', state.url);
  auth.debugTokenInfo();

  // 1. User is logged in?
  if (!auth.isLoggedIn()) {
    console.log('AuthGuard: User not logged in, redirecting to login');
    router.navigate(['/login']);
    return false;
  }

  console.log('AuthGuard: User is logged in');

  // 2. User is trying to access a protected route?
  if (state.url.startsWith('/admin')) {
    if (auth.getRole() !== 'ADMIN') {
      Swal.fire({
        title: 'Access Denied',
        text: 'You do not have admin access. Only administrators can access this area.',
        icon: 'error',
        confirmButtonColor: '#dc3545',
        confirmButtonText: 'OK',
        background: '#fff',
        customClass: {
          popup: 'swal-popup',
          title: 'swal-title'
        }
      });
      router.navigate(['/dashboard']);
      return false;
    }
  }

  return true;
};