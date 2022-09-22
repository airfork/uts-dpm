import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockPipe } from './pipes/BlockPipe';
import { NamePipe } from './pipes/NamePipe';
import { PointsPipe } from './pipes/PointsPipe';

@NgModule({
  declarations: [BlockPipe, NamePipe, PointsPipe],
  imports: [CommonModule],
  exports: [BlockPipe, NamePipe, PointsPipe],
})
export class SharedModule {}
