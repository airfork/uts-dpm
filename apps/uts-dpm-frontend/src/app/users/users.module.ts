import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UsersRoutingModule } from './users-routing.module';
import { UsersListComponent } from './users-list/users-list.component';
import { UsersComponent } from './users/users.component';
import { UiModule } from '../ui/ui.module';

@NgModule({
  declarations: [UsersListComponent, UsersComponent],
  imports: [CommonModule, UsersRoutingModule, UiModule],
})
export class UsersModule {}
