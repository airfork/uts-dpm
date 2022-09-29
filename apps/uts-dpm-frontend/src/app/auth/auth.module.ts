import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login/login.component';
import { AuthRoutingModule } from './auth-routing.module';
import { AuthComponent } from './auth/auth.component';
import { RippleModule } from 'primeng/ripple';
import { ReactiveFormsModule } from '@angular/forms';
import { RemoveIfUnauthorizedDirective } from './remove-if-unauthorized.directive';

@NgModule({
  declarations: [LoginComponent, AuthComponent, RemoveIfUnauthorizedDirective],
  imports: [CommonModule, AuthRoutingModule, RippleModule, ReactiveFormsModule],
  exports: [RemoveIfUnauthorizedDirective],
})
export class AuthModule {}
