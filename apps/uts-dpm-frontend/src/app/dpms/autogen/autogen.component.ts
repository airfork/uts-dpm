import { Component, Inject, LOCALE_ID, OnInit } from '@angular/core';
import { AutogenService } from '../../services/autogen.service';
import { formatDate } from '@angular/common';
import { NotificationService } from '../../services/notification.service';
import DPM from '../../models/dpm';

@Component({
  selector: 'app-autogen',
  templateUrl: './autogen.component.html',
  styleUrls: ['./autogen.component.scss'],
})
export class AutogenComponent implements OnInit {
  dpms?: DPM[];
  submittedTime?: String;

  constructor(
    private autogenService: AutogenService,
    @Inject(LOCALE_ID) private locale: string,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.autogenService
      .getAutogenDpms()
      .subscribe((value) => (this.dpms = value));
  }

  onSubmit() {
    this.notificationService.showSuccess('Submitted DPMs!', '');
    this.submittedTime = formatDate(new Date(), 'hh:mm', this.locale);
  }
}
