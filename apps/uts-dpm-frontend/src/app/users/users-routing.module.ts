import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UsersListComponent } from './users-list/users-list.component';
import { UsersComponent } from './users/users.component';
import { GenerateTitle } from '../shared/title-helper';
import { UserDetailComponent } from './user-detail/user-detail.component';
import { AuthGuard } from '../auth/auth.guard';

const routes: Routes = [
  {
    path: 'users',
    component: UsersComponent,
    canActivateChild: [AuthGuard],
    data: {
      allowedRoles: ['ADMIN'],
    },
    children: [
      {
        path: '',
        component: UsersListComponent,
        title: GenerateTitle('Users'),
        data: {
          allowedRoles: ['ADMIN'],
        },
      },
      {
        path: ':id',
        component: UserDetailComponent,
        title: GenerateTitle('User Details'),
        data: {
          allowedRoles: ['ADMIN'],
        },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class UsersRoutingModule {}
