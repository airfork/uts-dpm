import { Component } from '@angular/core';

interface navbarLinks {
  path: string;
  name: string;
}

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent {
  links: navbarLinks[] = [
    { path: '/dpm', name: 'DPM' },
    { path: '/autogen', name: 'Autogen' },
    { path: '/datagen', name: 'Datagen' },
    { path: '/approvals', name: 'Approvals' },
    { path: '/users', name: 'Users' },
    { path: '/message', name: 'Logout' },
  ];

  constructor() {}

  menuItemClick(): void {
    if (document.activeElement instanceof HTMLElement) {
      document.activeElement.blur();
    }
  }
}
