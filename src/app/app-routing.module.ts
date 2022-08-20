import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MessageBubbleComponent } from './components/message-bubble/message-bubble.component';
import { ClickerComponent } from './components/clicker/clicker.component';
import { HomeComponent } from './pages/home/home.component';

const titlePrefix = 'UTS DPM - ';

const routes: Routes = [
  { path: 'clicker', component: ClickerComponent },
  { path: 'message', component: MessageBubbleComponent },
  { path: '', component: HomeComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
