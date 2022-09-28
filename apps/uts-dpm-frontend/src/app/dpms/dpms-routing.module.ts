import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { DpmsComponent } from './dpms/dpms.component';
import { HomeComponent } from './home/home.component';
import { NewDpmComponent } from './new-dpm/new-dpm.component';
import { AutogenComponent } from './autogen/autogen.component';
import { DatagenComponent } from './datagen/datagen.component';
import { ApprovalsComponent } from './approvals/approvals.component';
import { GenerateTitle, TitlePrefix } from '../shared/titleHelper';
import { AuthGuard } from '../auth/auth.guard';

const routes: Routes = [
  {
    path: '',
    component: DpmsComponent,
    children: [
      {
        path: '',
        component: HomeComponent,
        title: TitlePrefix,
        canActivate: [AuthGuard],
        data: {
          allowedRoles: ['ADMIN', 'ANALYST', 'DRIVER', 'MANAGER', 'SUPERVISOR'],
        },
      },
      {
        path: 'dpm',
        component: NewDpmComponent,
        title: GenerateTitle('New DPM'),
        canActivate: [AuthGuard],
        data: {
          allowedRoles: ['ADMIN', 'ANALYST', 'MANAGER', 'SUPERVISOR'],
        },
      },
      {
        path: 'autogen',
        component: AutogenComponent,
        title: GenerateTitle('Autogenerate DPMs'),
        canActivate: [AuthGuard],
        data: {
          allowedRoles: ['ADMIN', 'ANALYST', 'MANAGER', 'SUPERVISOR'],
        },
      },
      {
        path: 'datagen',
        component: DatagenComponent,
        title: GenerateTitle('Generate Data'),
        canActivate: [AuthGuard],
        data: {
          allowedRoles: ['ADMIN', 'ANALYST', 'MANAGER'],
        },
      },
      {
        path: 'approvals',
        component: ApprovalsComponent,
        title: GenerateTitle('Approvals'),
        canActivate: [AuthGuard],
        data: {
          allowedRoles: ['ADMIN', 'MANAGER'],
        },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DpmsRoutingModule {}
