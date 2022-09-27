import { Component, Inject, LOCALE_ID, OnInit } from '@angular/core';
import { AutogenService } from '../../services/autogen.service';
import { formatDate } from '@angular/common';
import { NotificationService } from '../../services/notification.service';
import { first, Observable } from 'rxjs';
import AutogenDpm from '../../models/autogenDpm';

@Component({
  selector: 'app-autogen',
  templateUrl: './autogen.component.html',
  styleUrls: ['./autogen.component.scss'],
})
export class AutogenComponent implements OnInit {
  autogenDpms?: AutogenDpm[];
  submittedTime?: String;

  constructor(
    private autogenService: AutogenService,
    @Inject(LOCALE_ID) private locale: string,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.autogenService
      .getAutogenDpms()
      .pipe(first())
      .subscribe((wrapper) => {
        if (wrapper.submitted) this.submittedTime = wrapper.submitted;
        this.autogenDpms = wrapper.dpms;
      });
  }

  onSubmit() {
    this.autogenService
      .submit()
      .pipe(first())
      .subscribe(() => {
        this.notificationService.showSuccess('Submitted DPMs!', '');
        this.submittedTime = formatDate(new Date(), 'hhmm', this.locale);
      });
  }
}
