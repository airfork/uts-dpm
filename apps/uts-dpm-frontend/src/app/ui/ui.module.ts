import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageBubbleComponent } from './message-bubble/message-bubble.component';
import { NavbarComponent } from './navbar/navbar.component';
import { AppRoutingModule } from '../app-routing.module';
import { DpmListComponent } from './dpm-list/dpm-list.component';
import { LoadingComponent } from './loading/loading.component';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@NgModule({
  declarations: [
    MessageBubbleComponent,
    NavbarComponent,
    DpmListComponent,
    LoadingComponent,
  ],
  imports: [CommonModule, AppRoutingModule, ProgressSpinnerModule],
  exports: [
    MessageBubbleComponent,
    NavbarComponent,
    DpmListComponent,
    LoadingComponent,
  ],
})
export class UiModule {}
