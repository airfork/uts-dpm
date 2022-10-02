import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { UserService } from '../../services/user.service';
import UsernameDto from '../../models/username-dto';
import { FormatService } from '../../services/format.service';
import { first } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { ListTab } from '../shared/tab.types';
import {
  LIST_EMAIL_MESSAGE,
  LIST_RESET_MESSAGE,
  ListOutputKey,
} from '../shared/confirm-box-info';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersListComponent implements OnInit {
  users?: UsernameDto[];
  filteredUsers: UsernameDto[] = [];
  activeTab = { actions: false, create: false, search: true };
  managers: string[] | null = null;
  modalOpen = false;
  modalMessage = '';
  outputKey: ListOutputKey = 'email';

  constructor(
    private userService: UserService,
    private formatService: FormatService,
    private notificationService: NotificationService,
    private changeDetector: ChangeDetectorRef,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // jump to tab based on query param
    this.route.queryParamMap.pipe(first()).subscribe((value) => {
      const tab = value.get('tab') as ListTab;
      if (tab) this.activateTab(tab);
    });

    this.userService
      .getUserNames()
      .pipe(first())
      .subscribe((users) => {
        this.users = users;
        this.filteredUsers = users;
        this.changeDetector.detectChanges();
      });
  }

  activateTab(tab: ListTab) {
    switch (tab) {
      case 'actions':
        this.saveTabInUrl(tab);
        this.activeTab = { actions: true, create: false, search: false };
        break;

      case 'create':
        this.saveTabInUrl(tab);
        if (!this.managers) {
          this.userService
            .getManagers()
            .pipe(first())
            .subscribe((managers) => {
              this.managers = managers;
              this.changeDetector.detectChanges();
            });
        }
        this.activeTab = { actions: false, create: true, search: false };
        break;

      case 'search':
        this.saveTabInUrl(tab);
        this.activeTab = { actions: false, create: false, search: true };
        break;
      default:
        console.warn(`Unknown tab: ${tab}`);
        this.activeTab = { actions: false, create: false, search: true };
        this.clearQueryParams();
    }
  }

  filterUsers($event: Event) {
    if (!this.users) return;

    const target = $event.target as HTMLInputElement;
    this.filteredUsers = this.users?.filter((user) =>
      user.name.toLowerCase().includes(target.value.toLowerCase())
    );
  }

  handlerUserClick(id: number) {
    this.router.navigate([`/users/${id}`]);
  }

  sendEmailClick() {
    this.outputKey = 'email';
    this.modalMessage = LIST_EMAIL_MESSAGE;
    this.modalOpen = true;
  }

  resetPointsClick() {
    this.outputKey = 'reset';
    this.modalMessage = LIST_RESET_MESSAGE;
    this.modalOpen = true;
  }

  handleConfirmEvent($event: string) {
    switch ($event as ListOutputKey) {
      case 'email':
        console.log('Sending points email');
        break;
      case 'reset':
        this.resetPointBalances();
        break;
      default:
        console.warn('Unknown user list output event: ' + $event);
    }
  }

  get format() {
    return this.formatService;
  }

  private resetPointBalances() {
    this.userService
      .resetPointBalances()
      .pipe(first())
      .subscribe(() =>
        this.notificationService.showSuccess(
          'Part-timer point balances have been reset'
        )
      );
  }

  private saveTabInUrl(tab: ListTab) {
    // update query param to save tab state
    // need to set title in callback as it gets reset
    this.router.navigate(['.'], {
      relativeTo: this.route,
      queryParams: { tab },
      replaceUrl: true,
    });
  }

  private clearQueryParams() {
    this.router.navigate(['.'], {
      relativeTo: this.route,
      replaceUrl: true,
    });
  }
}
