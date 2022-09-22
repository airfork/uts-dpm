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
import { Router } from '@angular/router';

@Component({
  selector: 'app-users-list',
  templateUrl: './users-list.component.html',
  styleUrls: ['./users-list.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersListComponent implements OnInit {
  users?: UsernameDto[];
  filteredUsers: UsernameDto[] = [];
  searchVisible = true;

  constructor(
    private userService: UserService,
    private formatService: FormatService,
    private changeDetector: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit() {
    this.userService
      .getUserNames()
      .pipe(first())
      .subscribe((users) => {
        this.users = users;
        this.filteredUsers = users;
        this.changeDetector.detectChanges();
      });
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

  userTracking(index: number, user: UsernameDto) {
    return user.id;
  }

  get format() {
    return this.formatService;
  }
}
