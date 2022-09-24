import { Component, Inject, LOCALE_ID } from '@angular/core';
import { AutogenService } from '../../services/autogen.service';
import { formatDate } from '@angular/common';
import { NotificationService } from '../../services/notification.service';
import { Observable } from 'rxjs';
import AutogenDpm from '../../models/autogenDpm';

@Component({
  selector: 'app-autogen',
  templateUrl: './autogen.component.html',
  styleUrls: ['./autogen.component.scss'],
})
export class AutogenComponent {
  autogenDpms$: Observable<AutogenDpm[]>;
  submittedTime?: String;

  constructor(
    private autogenService: AutogenService,
    @Inject(LOCALE_ID) private locale: string,
    private notificationService: NotificationService
  ) {
    this.autogenDpms$ = this.autogenService.getAutogenDpms();
  }

  onSubmit() {
    this.notificationService.showSuccess('Submitted DPMs!', '');
    this.submittedTime = formatDate(new Date(), 'hhmm', this.locale);
  }
}
