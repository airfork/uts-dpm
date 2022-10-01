import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private router: Router, private authService: AuthService) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    if (this.authService.isAuthenticated()) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${this.authService.userData.token}`,
        },
      });
    }

    return next.handle(req).pipe(
      tap({
        next: () => {},
        error: (error) => {
          if (error instanceof HttpErrorResponse) {
            if (
              error.status === 401 &&
              !(
                req.url.includes('/api/auth/login') ||
                req.url.includes('/api/auth/changePassword')
              )
            ) {
              console.log('Not authorized, redirecting to login page');
              this.router.navigate(['/login']);
            } else if (error.status === 303) {
              console.info('Password needs to be changed, redirecting');
              this.router.navigate(['/passwordChange']);
            }
          }
        },
      })
    );
  }
}
