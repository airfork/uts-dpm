import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login/login.component';
import { AuthRoutingModule } from './auth-routing.module';
import { AuthComponent } from './auth/auth.component';
import { RippleModule } from 'primeng/ripple';
import { ReactiveFormsModule } from '@angular/forms';
import { RemoveIfUnauthorizedDirective } from './directives/remove-if-unauthorized.directive';
import { ChangePasswordComponent } from './change-password/change-password.component';
import { SharedModule } from '../shared/shared.module';

@NgModule({
  declarations: [
    LoginComponent,
    AuthComponent,
    RemoveIfUnauthorizedDirective,
    ChangePasswordComponent,
  ],
  imports: [
    CommonModule,
    AuthRoutingModule,
    RippleModule,
    ReactiveFormsModule,
    SharedModule,
  ],
  exports: [RemoveIfUnauthorizedDirective],
})
export class AuthModule {}
