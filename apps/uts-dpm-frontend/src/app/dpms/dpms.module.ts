import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './home/home.component';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NewDpmComponent } from './new-dpm/new-dpm.component';
import { FlatpickrModule } from 'angularx-flatpickr';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CalendarModule } from 'primeng/calendar';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { AutogenComponent } from './autogen/autogen.component';
import { DatagenComponent } from './datagen/datagen.component';
import { UiModule } from '../ui/ui.module';
import { ApprovalsComponent } from './approvals/approvals.component';
import { DpmsRoutingModule } from './dpms-routing.module';
import { DpmsComponent } from './dpms/dpms.component';
import { SharedModule } from '../shared/shared.module';
import { TableModule } from 'primeng/table';

@NgModule({
  declarations: [
    HomeComponent,
    NewDpmComponent,
    AutogenComponent,
    DatagenComponent,
    ApprovalsComponent,
    DpmsComponent,
  ],
  imports: [
    CommonModule,
    BrowserModule,
    FormsModule,
    FlatpickrModule.forRoot(),
    ReactiveFormsModule,
    AutoCompleteModule,
    BrowserAnimationsModule,
    CalendarModule,
    RippleModule,
    ToastModule,
    UiModule,
    DpmsRoutingModule,
    SharedModule,
    TableModule,
  ],
})
export class DpmsModule {}
