import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotFoundComponent } from './not-found/not-found.component';
import { GenerateTitle } from './shared/titleHelper';

const routes: Routes = [
  {
    path: '**',
    component: NotFoundComponent,
    title: GenerateTitle('Page Not Found'),
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
