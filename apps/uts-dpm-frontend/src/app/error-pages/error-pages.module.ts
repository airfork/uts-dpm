import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ErrorPagesComponent } from './error-pages/error-pages.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { ErrorPagesRoutingModule } from './error-pages-routing.module';
import { ErrorPageComponent } from './error-page/error-page.component';
import { ForbiddenComponent } from './forbidden/forbidden.component';

@NgModule({
  declarations: [
    ErrorPagesComponent,
    NotFoundComponent,
    ErrorPageComponent,
    ForbiddenComponent,
  ],
  imports: [CommonModule, ErrorPagesRoutingModule],
})
export class ErrorPagesModule {}
