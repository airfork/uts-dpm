import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './navbar/navbar.component';
import { AppRoutingModule } from '../app-routing.module';
import { AuthModule } from '../auth/auth.module';
import { ConfirmBoxComponent } from './confirm-box/confirm-box.component';
import { FormsModule } from '@angular/forms';
import { RippleModule } from 'primeng/ripple';

@NgModule({
  declarations: [NavbarComponent, ConfirmBoxComponent],
  imports: [
    CommonModule,
    AppRoutingModule,
    AuthModule,
    FormsModule,
    RippleModule,
  ],
  exports: [NavbarComponent, ConfirmBoxComponent],
})
export class UiModule {}
