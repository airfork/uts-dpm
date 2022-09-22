import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockPipe } from './pipes/BlockPipe';
import { NamePipe } from './pipes/NamePipe';

@NgModule({
  declarations: [BlockPipe, NamePipe],
  imports: [CommonModule],
  exports: [BlockPipe, NamePipe],
})
export class SharedModule {}
