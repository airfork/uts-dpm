import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
} from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import GetUserDetailDto from '../../models/get-user-detail-dto';
import UserDetailDto from '../../models/user-detail-dto';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import Required from '../../shared/required-decorator';
import { first } from 'rxjs';
import { NotificationService } from '../../services/notification.service';
import CreateUserDto from '../../models/create-user-dto';
import { HttpErrorResponse } from '@angular/common/http';

const POINTS_VALIDATORS = [Validators.required, Validators.pattern(/-?\d+/)];

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserFormComponent implements OnInit, OnChanges {
  @Input() @Required layout: 'create' | 'edit' = 'edit';
  @Input() managers: string[] | null = null;
  @Input() userInfo?: { user: GetUserDetailDto; id: string };
  user?: GetUserDetailDto;
  userId?: string;

  roles: string[] = [];

  userFormGroup = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    firstname: new FormControl('', [Validators.required]),
    lastname: new FormControl('', [Validators.required]),
    points: new FormControl(0, POINTS_VALIDATORS),
    manager: new FormControl(''),
    role: new FormControl(''),
    fullTime: new FormControl(false),
  });

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private changeDetector: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['managers'] && this.layout === 'create') {
      this.managers = changes['managers'].currentValue;
      this.setCreateFormData();
    }
  }

  ngOnInit() {
    if (this.layout === 'create') {
      this.initCreateLayout();
    } else {
      this.initEditLayout();
    }
  }

  onSubmit() {
    if (this.layout == 'edit') {
      this.editUser();
    } else {
      this.createUser();
    }
  }

  isButtonDisabled(): boolean {
    if (this.layout === 'edit') {
      return !this.userFormGroup.valid || this.userFormGroup.pristine;
    }

    return !this.userFormGroup.valid;
  }

  hasErrors(control: AbstractControl | null): boolean {
    if (!control) return false;
    return control.invalid && (control.dirty || control.touched);
  }

  setStatusClass(control: AbstractControl | null, isInput = true): string {
    if (control == null) return '';
    const prefix = isInput ? 'input-' : 'text-';

    if (this.hasErrors(control)) return prefix + 'error';
    if (this.layout === 'create' && (control.dirty || control.touched)) {
      return prefix + 'success';
    }
    return '';
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

  private setEditFormData() {
    if (!this.user) return;

    this.userFormGroup.reset({
      email: this.user.email,
      firstname: this.user.firstname,
      lastname: this.user.lastname,
      points: this.user.points,
      manager: this.managers![0],
      role: this.roles[0],
      fullTime: this.user.fullTime,
    });
  }

  private setCreateFormData() {
    if (this.managers) {
      this.userFormGroup.reset({
        manager: this.managers[0],
        role: this.roles[0],
      });
    } else {
      this.userFormGroup.reset({
        role: this.roles[0],
      });
    }
    this.points?.removeValidators(POINTS_VALIDATORS);
  }

  private formGroupToUserDetailDto(): UserDetailDto {
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

  private formGroupToCreateUserDto(): CreateUserDto {
    const values = this.userFormGroup.value;
    return {
      email: values.email!,
      firstname: values.firstname!,
      lastname: values.lastname!,
      manager: values.manager!,
      role: values.role!,
      fullTime: values.fullTime!,
    };
  }

  private initCreateLayout() {
    this.roles = this.userService.orderRoles('Driver');
    this.setCreateFormData();
    this.changeDetector.detectChanges();
  }

  private initEditLayout() {
    if (!this.userInfo) {
      console.error('User and id must be passed in if using the edit layout');
      return;
    }

    this.user = this.userInfo.user;
    this.userId = this.userInfo.id;
    this.roles = this.userService.orderRoles(this.user.role);
    this.managers = this.userService.orderManagers(
      this.user.manager,
      this.user.managers
    );

    if (
      this.authService.userData.username.toLowerCase() ===
      this.user.email.toLowerCase()
    ) {
      this.userFormGroup.get('role')!.disable();
    }

    this.setEditFormData();
  }

  private editUser() {
    this.userService
      .updateUser(this.formGroupToUserDetailDto(), this.userId!)
      .pipe(first())
      .subscribe(() => {
        this.notificationService.showSuccess('User updated', '');
        const values = this.userFormGroup.value;

        // role is not set if the formControl is disabled
        // role formControl is disabled if user is viewing themselves
        if (!values.role) {
          this.userFormGroup.reset({ ...values, role: this.roles[0] });
        } else {
          this.userFormGroup.reset({ ...values });
        }
      });
  }

  private createUser() {
    this.userService
      .createUser(this.formGroupToCreateUserDto())
      .pipe(first())
      .subscribe({
        next: () => {
          this.notificationService.showSuccess('User created', '');
          this.userFormGroup.reset({
            manager: this.managers![0],
            role: this.roles![0],
          });
          this.changeDetector.detectChanges();
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 422) {
            this.notificationService.showError(
              'A user with this email/username already exists',
              'Error'
            );
            const values = this.userFormGroup.value;
            this.userFormGroup.reset({ ...values, email: '' });
            this.firstname?.markAsTouched();
            this.lastname?.markAsTouched();
            this.changeDetector.detectChanges();
          } else {
            this.notificationService.showError(
              'Something went wrong, please try again',
              'Error'
            );
            console.error(
              'Something went wrong trying to create the user',
              error
            );
          }
        },
      });
  }
}
