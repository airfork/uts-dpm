import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import GetUserDetailDto from '../../models/getUserDetailDto';
import { UserDetailService } from '../../services/user-detail.service';
import { first } from 'rxjs';
import { Title } from '@angular/platform-browser';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import UserDetailDto from '../../models/userDetailDto';
import { NotificationService } from '../../services/notification.service';
import DpmDetailDto from '../../models/dpmDetailDto';
import { LazyLoadEvent } from 'primeng/api';
import { DpmService } from '../../services/dpm.service';
import { ApprovalsService } from '../../services/approvals.service';
import { AuthService } from '../../services/auth.service';

type tab = 'actions' | 'dpms' | 'info';

@Component({
  selector: 'app-user-detail',
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.scss'],
})
export class UserDetailComponent implements OnInit {
  private userId = '';
  private lastLazyLoadEvent?: LazyLoadEvent;

  loadingDpms = true;
  totalRecords = 0;
  activeTab = { info: true, dpms: false, actions: false };
  user?: GetUserDetailDto;
  roles: string[] = [];
  managers: string[] = [];
  currentDpm?: DpmDetailDto;
  modalOpen = false;
  dpms: DpmDetailDto[] = [];

  userFormGroup = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    firstname: new FormControl('', [Validators.required]),
    lastname: new FormControl('', [Validators.required]),
    points: new FormControl(0, [
      Validators.required,
      Validators.pattern(/-?\d+/),
    ]),
    manager: new FormControl(''),
    role: new FormControl(''),
    fullTime: new FormControl(false),
  });

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private userDetailService: UserDetailService,
    private titleService: Title,
    private notificationService: NotificationService,
    private dpmService: DpmService,
    private approvalsService: ApprovalsService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.route.params.pipe(first()).subscribe((value) => {
      const { id } = value as { id: string };
      this.userDetailService
        .getUser(id)
        .pipe(first())
        .subscribe((user) => {
          this.user = user;
          this.setTitle();
          this.userId = id;
          this.roles = this.userDetailService.orderRoles(user.role);
          this.managers = this.userDetailService.orderManagers(
            user.manager,
            user.managers
          );
          this.setFormData();

          if (
            this.authService.userData.username.toLowerCase() ===
            user.email.toLowerCase()
          ) {
            this.userFormGroup.get('role')!.disable();
          }
        });
    });

    // jump to tab based on query param
    this.route.queryParamMap.pipe(first()).subscribe((value) => {
      const tab = value.get('tab') as tab;
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

  activateTab(tab: tab) {
    switch (tab) {
      case 'actions':
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

  onSubmit() {
    this.userDetailService
      .updateUser(this.formGroupToDto(), this.userId)
      .pipe(first())
      .subscribe(() => {
        this.notificationService.showSuccess('User updated', '');
        const values = this.userFormGroup.value;
        this.userFormGroup.reset({ ...values });
      });
  }

  hasErrors(control: AbstractControl | null): boolean {
    if (!control) return false;
    return control.invalid && (control.dirty || control.touched);
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

  getEmailValidationMessages(): string {
    if (!this.hasErrors(this.email)) return '';

    if (this.email?.errors?.['required']) {
      return 'Email is required';
    }

    if (this.email?.errors?.['email']) {
      return 'Input is not a valid email address';
    }

    return '';
  }

  getFirstnameValidationMessages(): string {
    if (!this.hasErrors(this.firstname)) return '';

    if (this.firstname?.errors?.['required']) {
      return 'First name is required';
    }

    return '';
  }

  getLastnameValidationMessages(): string {
    if (!this.hasErrors(this.lastname)) return '';

    if (this.lastname?.errors?.['required']) {
      return 'Last name is required';
    }

    return '';
  }

  getPointsValidationMessages(): string {
    if (!this.hasErrors(this.points)) return '';

    if (this.points?.errors?.['required']) {
      return 'Points is required';
    }

    if (this.points?.errors?.['pattern']) {
      return 'Points must be a valid number';
    }

    return '';
  }

  get email() {
    return this.userFormGroup.get('email');
  }

  get firstname() {
    return this.userFormGroup.get('firstname');
  }

  get lastname() {
    return this.userFormGroup.get('lastname');
  }

  get points() {
    return this.userFormGroup.get('points');
  }

  private setFormData() {
    if (!this.user) return;

    this.userFormGroup.reset({
      email: this.user.email,
      firstname: this.user.firstname,
      lastname: this.user.lastname,
      points: this.user.points,
      manager: this.managers[0],
      role: this.roles[0],
      fullTime: this.user.fullTime,
    });
  }

  private formGroupToDto(): UserDetailDto {
    const values = this.userFormGroup.value;
    return {
      email: values.email!,
      firstname: values.firstname!,
      lastname: values.lastname!,
      points: values.points!,
      manager: values.manager!,
      role: values.role!,
      fullTime: values.fullTime!,
    };
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

  private saveTabInUrl(tab: tab) {
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
}
