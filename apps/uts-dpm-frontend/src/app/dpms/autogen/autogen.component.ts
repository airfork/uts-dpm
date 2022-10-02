import { Component, Inject, LOCALE_ID, OnInit } from '@angular/core';
import { AutogenService } from '../../services/autogen.service';
import { formatDate } from '@angular/common';
import { NotificationService } from '../../services/notification.service';
import { first } from 'rxjs';
import AutogenDpm from '../../models/autogen-dpm';

@Component({
  selector: 'app-autogen',
  templateUrl: './autogen.component.html',
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
        this.notificationService.showSuccess('Submitted DPMs!');
        this.submittedTime = formatDate(new Date(), 'HHmm', this.locale);
      });
  }
}
