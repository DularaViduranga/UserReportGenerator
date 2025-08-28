import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();
  
  console.log('AuthInterceptor intercepting request to:', req.url);
  console.log('Token exists:', !!token);
  console.log('Token value (first 20 chars):', token ? token.substring(0, 20) + '...' : 'null');
  
  if (token) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    console.log('Added Authorization header to request');
    console.log('Request headers:', authReq.headers.keys());
    return next(authReq);
  }
  
  console.log('No token available, sending request without Authorization header');
  return next(req);
};
