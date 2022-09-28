import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavbarComponent } from './navbar/navbar.component';
import { AppRoutingModule } from '../app-routing.module';
import { LoadingComponent } from './loading/loading.component';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { AuthModule } from '../auth/auth.module';

@NgModule({
  declarations: [NavbarComponent, LoadingComponent],
  imports: [CommonModule, AppRoutingModule, ProgressSpinnerModule, AuthModule],
  exports: [NavbarComponent, LoadingComponent],
})
export class UiModule {}
