import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ErrorPagesComponent } from './error-pages/error-pages.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { GenerateTitle } from '../shared/title-helper';
import { ForbiddenComponent } from './forbidden/forbidden.component';

const routes: Routes = [
  {
    path: 'errors',
    component: ErrorPagesComponent,
    children: [
      {
        path: '403',
        component: ForbiddenComponent,
        title: GenerateTitle('403'),
      },
      {
        path: '404',
        component: NotFoundComponent,
        title: GenerateTitle('404'),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ErrorPagesRoutingModule {}
