import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockPipe } from './pipes/BlockPipe';
import { NamePipe } from './pipes/NamePipe';
import { PointsPipe } from './pipes/PointsPipe';
import { LoadingComponent } from './loading/loading.component';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@NgModule({
  declarations: [BlockPipe, NamePipe, PointsPipe, LoadingComponent],
  imports: [CommonModule, ProgressSpinnerModule],
  exports: [BlockPipe, NamePipe, PointsPipe, LoadingComponent],
})
export class SharedModule {}
