import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './navbar/navbar.component';
import { AppRoutingModule } from '../app-routing.module';
import { AuthModule } from '../auth/auth.module';

@NgModule({
  declarations: [NavbarComponent],
  imports: [CommonModule, AppRoutingModule, AuthModule],
  exports: [NavbarComponent],
})
export class UiModule {}
