import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UsersRoutingModule } from './users-routing.module';
import { UsersListComponent } from './users-list/users-list.component';
import { UsersComponent } from './users/users.component';
import { UiModule } from '../ui/ui.module';
import { SharedModule } from '../shared/shared.module';
import { UserDetailComponent } from './user-detail/user-detail.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';

@NgModule({
  declarations: [UsersListComponent, UsersComponent, UserDetailComponent],
  imports: [
    CommonModule,
    UsersRoutingModule,
    UiModule,
    SharedModule,
    ReactiveFormsModule,
    RippleModule,
    TableModule,
    InputTextModule,
  ],
})
export class UsersModule {}
