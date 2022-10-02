import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import GetUserDetailDto from '../../models/get-user-detail-dto';
import { first } from 'rxjs';
import { Title } from '@angular/platform-browser';
import { NotificationService } from '../../services/notification.service';
import DpmDetailDto from '../../models/dpm-detail-dto';
import { LazyLoadEvent } from 'primeng/api';
import { DpmService } from '../../services/dpm.service';
import { ApprovalsService } from '../../services/approvals.service';
import { UserService } from '../../services/user.service';
import { DetailTab } from '../shared/tab.types';
import {
  DETAIL_DELETE_MESSAGE,
  DETAIL_EMAIL_MESSAGE,
  DETAIL_RESET_MESSAGE,
  DetailOutputKey,
} from '../shared/confirm-box-info';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-user-detail',
  templateUrl: './user-detail.component.html',
})
export class UserDetailComponent implements OnInit {
  private lastLazyLoadEvent?: LazyLoadEvent;

  userId = '';
  loadingDpms = true;
  totalRecords = 0;
  activeTab = { info: true, dpms: false, actions: false };
  user?: GetUserDetailDto;
  currentDpm?: DpmDetailDto;
  dpms: DpmDetailDto[] = [];
  modalOpen = false;
  modalMessage = '';
  outputKey: DetailOutputKey = 'email';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private userService: UserService,
    private titleService: Title,
    private notificationService: NotificationService,
    private dpmService: DpmService,
    private approvalsService: ApprovalsService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.route.params.pipe(first()).subscribe((value) => {
      const { id } = value as { id: string };
      this.userService
        .getUser(id)
        .pipe(first())
        .subscribe((user) => {
          this.user = user;
          this.setTitle();
          this.userId = id;
        });
    });

    // jump to tab based on query param
    this.route.queryParamMap.pipe(first()).subscribe((value) => {
      const tab = value.get('tab') as DetailTab;
      if (tab) this.activateTab(tab);
    });
  }

  clickRow(dpm: DpmDetailDto) {
    this.currentDpm = dpm;
    this.modalOpen = true;
  }

  denyDpm() {
    if (!this.currentDpm) return;
    this.approvalsService
      .denyDpm(this.currentDpm.id)
      .pipe(first())
      .subscribe(() => {
        this.notificationService.showSuccess('DPM has been denied', 'Success');
        this.lazyLoadEvent(this.lastLazyLoadEvent!);
      });
  }

  activateTab(tab: DetailTab) {
    switch (tab) {
      case 'detail-actions':
        this.saveTabInUrl(tab);
        this.activeTab = { info: false, dpms: false, actions: true };
        break;
      case 'dpms':
        this.saveTabInUrl(tab);
        this.activeTab = { info: false, dpms: true, actions: false };
        break;
      case 'info':
        this.saveTabInUrl(tab);
        this.activeTab = { info: true, dpms: false, actions: false };
        break;
      default:
        console.error(`Unknown tab: ${tab}`);
    }
  }

  lazyLoadEvent(event: LazyLoadEvent) {
    this.lastLazyLoadEvent = event;
    this.loadingDpms = true;
    let size = 10;
    if (event.rows) size = event.rows;

    let page = 0;
    if (event.first) {
      page = event.first / size;
    }

    this.dpmService
      .getAll(this.userId, page, size)
      .pipe(first())
      .subscribe((page) => {
        this.dpms = page.content;
        this.totalRecords = page.totalElements;
        this.loadingDpms = false;
      });
  }

  handleConfirmEvent($event: string) {
    switch ($event as DetailOutputKey) {
      case 'delete':
        this.deleteUser();
        break;
      case 'email':
        console.log('Sending this user a points balance email');
        break;
      case 'reset':
        console.log("Resetting this user's password");
        break;
      default:
        console.warn('Unknown user detail output key: ' + $event);
    }
  }

  deleteUserClick() {
    // shouldn't happen, but just in case
    if (this.viewingSelf()) {
      this.notificationService.showWarning("You can't delete yourself -_-");
      return;
    }
    this.modalMessage = DETAIL_DELETE_MESSAGE;
    this.outputKey = 'delete';
    this.modalOpen = true;
  }

  sendEmailClick() {
    this.modalMessage = DETAIL_EMAIL_MESSAGE;
    this.outputKey = 'email';
    this.modalOpen = true;
  }

  resetClick() {
    this.modalMessage = DETAIL_RESET_MESSAGE;
    this.outputKey = 'reset';
    this.modalOpen = true;
  }

  viewingSelf(): boolean {
    return (
      this.authService.userData.username.toLowerCase().trim() ===
      this.user?.email.toLowerCase().trim()
    );
  }

  private setTitle() {
    if (this.user) {
      this.titleService.setTitle(
        `${this.titleService.getTitle()} (${this.user.firstname} ${
          this.user.lastname
        })`
      );
    }
  }

  private saveTabInUrl(tab: DetailTab) {
    // update query param to save tab state
    // need to set title in callback as it gets reset
    this.router
      .navigate(['.'], {
        relativeTo: this.route,
        queryParams: { tab },
        replaceUrl: true,
      })
      .then(() => this.setTitle());
  }

  private deleteUser() {
    this.userService
      .deleteUser(this.userId)
      .pipe(first())
      .subscribe(() => {
        this.router
          .navigate(['/users'])
          .then(() =>
            this.notificationService.showSuccess('User has been deleted')
          );
      });
  }
}
