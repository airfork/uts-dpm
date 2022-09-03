import { Component, OnInit } from '@angular/core';
import DPM from '../../models/dpm';
import { ApprovalService } from '../../services/approval.service';
import { FormatService } from '../../services/format.service';

@Component({
  selector: 'app-approvals',
  templateUrl: './approvals.component.html',
  styleUrls: ['./approvals.component.scss'],
})
export class ApprovalsComponent implements OnInit {
  dpms?: DPM[];
  modalOpen = false;
  currentDpm?: DPM;
  editOpen = false;
  currentPoints? = 0;

  constructor(
    private approvalsService: ApprovalService,
    private formatService: FormatService
  ) {}

  ngOnInit() {
    this.approvalsService
      .getApprovalDpms()
      .subscribe((value) => (this.dpms = value));
  }

  showApprovalModal = (dpm: DPM) => {
    this.currentDpm = dpm;
    this.modalOpen = true;
  };

  hideEdit() {
    if (this.currentPoints) {
      this.currentDpm!.points = this.currentPoints;
    }
    this.editOpen = false;
  }

  showEdit($event: MouseEvent) {
    $event.stopPropagation();
    this.currentPoints = this.currentDpm?.points;
    this.editOpen = true;
  }

  get format() {
    return this.formatService;
  }
}
