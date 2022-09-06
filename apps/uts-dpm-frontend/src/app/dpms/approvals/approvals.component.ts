import { Component, OnInit } from '@angular/core';
import DPM from '../../models/dpm';
import { ApprovalService } from '../../services/approval.service';
import { FormatService } from '../../services/format.service';
import { Observable } from 'rxjs';
import ApprovalDpmDto from '../../models/approvalDpmDto';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-approvals',
  templateUrl: './approvals.component.html',
  styleUrls: ['./approvals.component.scss'],
})
export class ApprovalsComponent implements OnInit {
  dpms: ApprovalDpmDto[] = [];
  modalOpen = false;
  currentDpm?: ApprovalDpmDto;
  editOpen = false;
  currentPoints? = 0;

  constructor(
    private approvalsService: ApprovalService,
    private formatService: FormatService,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.approvalsService
      .getApprovalDpms()
      .subscribe((value) => (this.dpms = value));
  }

  showApprovalModal(dpm: ApprovalDpmDto) {
    this.currentDpm = dpm;
    this.modalOpen = true;
  }

  hideEdit() {
    this.editOpen = false;
    if (this.currentPoints) {
      this.currentDpm!.points = this.currentPoints;
      this.approvalsService
        .updatePoints(this.currentDpm!.id, this.currentPoints)
        .subscribe();
    }
  }

  showEdit($event: MouseEvent) {
    $event.stopPropagation();
    this.currentPoints = this.currentDpm?.points;
    this.editOpen = true;
  }

  approveDpm() {
    if (!this.currentDpm) return;
    this.dpms = this.dpms.filter((dto) => dto.id != this.currentDpm?.id);
    this.approvalsService.approveDpm(this.currentDpm?.id).subscribe(() => {
      this.notificationService.showSuccess('DPM has been approved', 'Success');
    });
  }

  denyDpm() {
    if (!this.currentDpm) return;
    this.dpms = this.dpms.filter((dto) => dto.id != this.currentDpm?.id);
    this.approvalsService.denyDpm(this.currentDpm?.id).subscribe(() => {
      this.notificationService.showSuccess('DPM has been denied', 'Success');
    });
  }

  get format() {
    return this.formatService;
  }
}
