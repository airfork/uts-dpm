import { RouterModule, Routes } from '@angular/router';
import { NgModule } from '@angular/core';
import { DpmsComponent } from './dpms/dpms.component';
import { MessageBubbleComponent } from '../ui/message-bubble/message-bubble.component';
import { HomeComponent } from './home/home.component';
import { NewDpmComponent } from './new-dpm/new-dpm.component';
import { AutogenComponent } from './autogen/autogen.component';
import { DatagenComponent } from './datagen/datagen.component';
import { ApprovalsComponent } from './approvals/approvals.component';
import { GenerateTitle, TitlePrefix } from '../shared/titleHelper';

const routes: Routes = [
  {
    path: '',
    component: DpmsComponent,
    children: [
      {
        path: 'message',
        component: MessageBubbleComponent,
        title: GenerateTitle('Message'),
      },
      {
        path: '',
        component: HomeComponent,
        title: TitlePrefix,
      },
      {
        path: 'dpm',
        component: NewDpmComponent,
        title: GenerateTitle('New DPM'),
      },
      {
        path: 'autogen',
        component: AutogenComponent,
        title: GenerateTitle('Autogenerate DPMs'),
      },
      {
        path: 'datagen',
        component: DatagenComponent,
        title: GenerateTitle('Generate Data'),
      },
      {
        path: 'approvals',
        component: ApprovalsComponent,
        title: GenerateTitle('Approvals'),
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DpmsRoutingModule {}
