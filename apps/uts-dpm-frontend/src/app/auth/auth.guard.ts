import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateChild,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { Observable } from 'rxjs';
import { Roles } from './roles.types';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate, CanActivateChild {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ):
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree>
    | boolean
    | UrlTree {
    return this.hasPermissions(route.data['allowedRoles'], state.url);
  }

  canActivateChild(
    childRoute: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ):
    | boolean
    | UrlTree
    | Observable<boolean | UrlTree>
    | Promise<boolean | UrlTree> {
    return this.canActivate(childRoute, state);
  }

  private hasPermissions(allowedRoles: Roles[], url: string): boolean {
    if (this.authService.isAuthenticated()) {
      if (url.includes('/login')) {
        this.router.navigate(['/']);
        return false;
      }

      if (allowedRoles.includes(this.authService.userData.role as Roles)) {
        return true;
      }

      this.router.navigate(['/errors/403']);
      return false;
    }

    if (url.includes('/login')) return true;
    this.router.navigate(['/login']);
    return false;
  }
}
