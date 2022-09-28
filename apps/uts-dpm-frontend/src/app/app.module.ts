import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { DpmsModule } from './dpms/dpms.module';
import { RippleModule } from 'primeng/ripple';
import { ToastrModule } from 'ngx-toastr';
import { UsersRoutingModule } from './users/users-routing.module';
import { DpmsRoutingModule } from './dpms/dpms-routing.module';
import { UsersModule } from './users/users.module';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { AuthRoutingModule } from './auth/auth-routing.module';
import { AuthModule } from './auth/auth.module';
import { AuthInterceptor } from './auth/auth.interceptor';
import { ErrorPagesRoutingModule } from './error-pages/error-pages-routing.module';
import { ErrorPagesModule } from './error-pages/error-pages.module';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    ErrorPagesRoutingModule,
    AuthRoutingModule,
    DpmsRoutingModule,
    UsersRoutingModule,
    AppRoutingModule,
    ErrorPagesModule,
    AuthModule,
    DpmsModule,
    UsersModule,
    RippleModule,
    HttpClientModule,
    ToastrModule.forRoot({
      timeOut: 1000 * 3,
      positionClass: 'app-toast-top-center',
    }),
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
