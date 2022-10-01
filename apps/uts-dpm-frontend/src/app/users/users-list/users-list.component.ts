import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { UserService } from '../../services/user.service';
import UsernameDto from '../../models/usernameDto';
import { FormatService } from '../../services/format.service';
import { first } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';

type tab = 'actions' | 'create' | 'search';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersListComponent implements OnInit {
  users?: UsernameDto[];
  filteredUsers: UsernameDto[] = [];
  activeTab = { actions: false, create: false, search: true };
  managers: string[] | null = null;

  constructor(
    private userService: UserService,
    private formatService: FormatService,
    private changeDetector: ChangeDetectorRef,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // jump to tab based on query param
    this.route.queryParamMap.pipe(first()).subscribe((value) => {
      const tab = value.get('tab') as tab;
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

  activateTab(tab: tab) {
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

  get format() {
    return this.formatService;
  }

  private saveTabInUrl(tab: tab) {
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
