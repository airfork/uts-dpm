import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { first } from 'rxjs';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { PasswordsEqualValidator } from '../directives/passwords-equal.directive';
import { PasswordsNotEqualValidator } from '../directives/passwords-not-equal.directive';
import ChangePasswordDto from '../../models/change-password-dto';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
})
export class ChangePasswordComponent implements OnInit {
  isLoading = true;
  changePasswordFormGroup = new FormGroup(
    {
      currentPassword: new FormControl('', [Validators.required]),
      newPassword: new FormControl('', [
        Validators.required,
        Validators.minLength(8),
      ]),
      confirmPassword: new FormControl('', [Validators.required]),
    },
    { validators: [PasswordsEqualValidator, PasswordsNotEqualValidator] }
  );

  constructor(
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.authService
      .changePasswordRequired()
      .pipe(first())
      .subscribe((required) => {
        this.isLoading = false;
        if (!required) {
          this.router.navigate(['/']).then(() => {
            this.notificationService.showWarning(
              'Cannot change password currently'
            );
          });
        }
      });
  }

  onSubmit() {
    this.authService
      .changePassword(this.formGroupToDto())
      .pipe(first())
      .subscribe({
        next: () => {
          this.changePasswordFormGroup.reset();
          this.router
            .navigate(['/'])
            .then(() =>
              this.notificationService.showSuccess('Password has been changed')
            );
        },
        error: (error: HttpErrorResponse) => {
          const values = this.changePasswordFormGroup.value;
          if (error.status === 401) {
            this.notificationService.showError(
              'Current password is incorrect',
              'Error'
            );
            this.changePasswordFormGroup.reset({
              ...values,
              currentPassword: '',
            });
            this.newPassword?.markAsTouched();
            this.confirmPassword?.markAsTouched();
          } else if (error.status === 422) {
            this.notificationService.showError(
              'Please check the new and confirm password fields again',
              'Error'
            );
            this.changePasswordFormGroup.reset({
              currentPassword: values.currentPassword,
            });
            this.currentPassword?.markAsTouched();
          } else {
            console.error(error);
            this.notificationService.showError(
              'Something went wrong. please try again',
              'Error'
            );
          }
        },
      });
  }

  formGroupToDto(): ChangePasswordDto {
    const values = this.changePasswordFormGroup.value;
    return {
      currentPassword: values.currentPassword!,
      newPassword: values.newPassword!,
      confirmPassword: values.confirmPassword!,
    };
  }

  hasErrors(control: AbstractControl | null): boolean {
    if (!control) return false;
    return control.invalid && (control.dirty || control.touched);
  }

  setStatusClass(control: AbstractControl | null, isInput = true): string {
    if (control == null) return '';
    const prefix = isInput ? 'input-' : 'text-';

    if (this.hasErrors(control)) return prefix + 'error';

    if (
      control === this.confirmPassword &&
      this.getConfirmPasswordValidationMessages() !== ''
    ) {
      return prefix + 'error';
    }

    if (
      control === this.newPassword &&
      this.getNewPasswordValidationMessages() !== ''
    ) {
      return prefix + 'error';
    }

    if (control.dirty || control.touched) return prefix + 'success';
    return '';
  }

  getCurrentPasswordValidationMessages(): string {
    if (!this.hasErrors(this.currentPassword)) return '';

    if (this.currentPassword?.errors?.['required']) {
      return 'Current password is required';
    }

    return '';
  }

  getNewPasswordValidationMessages(): string {
    if (
      this.hasErrors(this.newPassword) &&
      this.newPassword?.errors?.['required']
    ) {
      return 'New password is required';
    }

    if (this.newPassword?.errors?.['minlength']) {
      return 'New password must be at least 8 characters long';
    }

    if (this.changePasswordFormGroup?.errors?.['passwordsNotEqual']) {
      return 'Password cannot be the same as your current password';
    }

    return '';
  }

  getConfirmPasswordValidationMessages(): string {
    if (
      this.hasErrors(this.confirmPassword) &&
      this.confirmPassword?.errors?.['required']
    ) {
      return 'Confirm password is required';
    }

    if (this.changePasswordFormGroup?.errors?.['passwordsEqual']) {
      return 'Password does not match new password';
    }

    return '';
  }

  get currentPassword() {
    return this.changePasswordFormGroup.get('currentPassword');
  }

  get newPassword() {
    return this.changePasswordFormGroup.get('newPassword');
  }

  get confirmPassword() {
    return this.changePasswordFormGroup.get('confirmPassword');
  }
}
