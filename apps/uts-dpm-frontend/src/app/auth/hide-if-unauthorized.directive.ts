import { Directive, ElementRef, Input, OnInit } from '@angular/core';
import { Roles } from './roles.types';
import { AuthService } from '../services/auth.service';

@Directive({
  selector: '[appHideIfUnauthorized]',
})
export default class HideIfUnauthorizedDirective implements OnInit {
  @Input('appHideIfUnauthorized') roles: Roles[] = [];

  constructor(private authService: AuthService, private el: ElementRef) {}

  ngOnInit() {
    const role = this.authService.userData.role as Roles;
    if (!this.roles.includes(role)) {
      this.el.nativeElement.remove();
    }
  }
}
