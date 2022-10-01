import { Directive, ElementRef, Input, OnInit } from '@angular/core';
import { Roles } from '../roles.types';
import { AuthService } from '../../services/auth.service';

@Directive({
  selector: '[appRemoveIfUnauthorized]',
})
export class RemoveIfUnauthorizedDirective implements OnInit {
  @Input('appRemoveIfUnauthorized') roles: Roles[] = [];

  constructor(private authService: AuthService, private el: ElementRef) {}

  ngOnInit() {
    const role = this.authService.userData.role as Roles;
    if (!this.roles.includes(role)) {
      this.el.nativeElement.remove();
    }
  }
}
