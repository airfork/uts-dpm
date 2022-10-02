import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { Roles } from '../../auth/roles.types';

interface navbarLinks {
  path?: string;
  name: string;
  allowedRoles: Roles[];
}

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
})
export class NavbarComponent {
  links: navbarLinks[] = [
    {
      path: '/dpm',
      name: 'DPM',
      allowedRoles: ['ADMIN', 'ANALYST', 'MANAGER', 'SUPERVISOR'],
    },
    {
      path: '/autogen',
      name: 'Autogen',
      allowedRoles: ['ADMIN', 'ANALYST', 'MANAGER', 'SUPERVISOR'],
    },
    {
      path: '/datagen',
      name: 'Datagen',
      allowedRoles: ['ADMIN', 'ANALYST', 'MANAGER'],
    },
    {
      path: '/approvals',
      name: 'Approvals',
      allowedRoles: ['ADMIN', 'MANAGER'],
    },
    { path: '/users', name: 'Users', allowedRoles: ['ADMIN'] },
    {
      name: 'Logout',
      allowedRoles: ['ADMIN', 'ANALYST', 'DRIVER', 'MANAGER', 'SUPERVISOR'],
    },
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  menuItemClick() {
    if (document.activeElement instanceof HTMLElement) {
      document.activeElement.blur();
    }
  }

  logoutClick() {
    this.menuItemClick();
    this.authService.logout();
    this.router
      .navigate(['/login'])
      .then(() => this.notificationService.showInfo('Logged out'));
  }
}
