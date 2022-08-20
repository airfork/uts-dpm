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
    { path: '/clicker', name: 'DPM' },
    { path: '/message', name: 'Autogen' },
    { path: '/message', name: 'Datagen' },
    { path: '/message', name: 'Approvals' },
    { path: '/message', name: 'Users' },
    { path: '/message', name: 'Logout' },
  ];

  constructor() {}
}
