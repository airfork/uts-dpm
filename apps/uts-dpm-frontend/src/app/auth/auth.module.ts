import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login/login.component';
import { AuthRoutingModule } from './auth-routing.module';
import { AuthComponent } from './auth/auth.component';
import { RippleModule } from 'primeng/ripple';
import { ReactiveFormsModule } from '@angular/forms';
import HideIfUnauthorizedDirective from './hide-if-unauthorized.directive';

@NgModule({
  declarations: [LoginComponent, AuthComponent, HideIfUnauthorizedDirective],
  imports: [CommonModule, AuthRoutingModule, RippleModule, ReactiveFormsModule],
  exports: [HideIfUnauthorizedDirective],
})
export class AuthModule {}
