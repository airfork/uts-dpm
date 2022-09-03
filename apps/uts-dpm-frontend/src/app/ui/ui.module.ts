import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageBubbleComponent } from './message-bubble/message-bubble.component';
import { NavbarComponent } from './navbar/navbar.component';
import { AppRoutingModule } from '../app-routing.module';
import { DpmListComponent } from './dpm-list/dpm-list.component';

@NgModule({
  declarations: [MessageBubbleComponent, NavbarComponent, DpmListComponent],
  imports: [CommonModule, AppRoutingModule],
  exports: [MessageBubbleComponent, NavbarComponent, DpmListComponent],
})
export class UiModule {}
