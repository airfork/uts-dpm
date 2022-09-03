import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { DpmsModule } from './dpms/dpms.module';
import { RippleModule } from 'primeng/ripple';
import { ToastrModule } from 'ngx-toastr';
import { UsersRoutingModule } from './users/users-routing.module';
import { NotFoundComponent } from './not-found/not-found.component';
import { DpmsRoutingModule } from './dpms/dpms-routing.module';
import { UsersModule } from './users/users.module';

@NgModule({
  declarations: [AppComponent, NotFoundComponent],
  imports: [
    BrowserModule,
    DpmsRoutingModule,
    UsersRoutingModule,
    AppRoutingModule,
    DpmsModule,
    UsersModule,
    RippleModule,
    ToastrModule.forRoot({
      timeOut: 3000,
      positionClass: 'app-toast-top-center',
    }),
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
