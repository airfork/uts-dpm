import { Component } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  Validators,
} from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent {
  badCredentials = false;
  loginFormGroup = new FormGroup({
    username: new FormControl('', [Validators.required]),
    password: new FormControl('', [Validators.required]),
  });

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  onSubmit() {
    const values = this.loginFormGroup.value;
    this.authService.login(values.username!, values.password!).subscribe({
      next: ({ token }) => {
        this.authService.saveToken(token);
        this.loginFormGroup.reset();
        this.router.navigate(['/']);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 401) this.badCredentials = true;
        else {
          console.error(error);
          this.notificationService.showError(
            'Something went wrong, please try again',
            'Error'
          );
        }
      },
    });
  }

  getUsernameValidationMessages(): string {
    if (!this.hasErrors(this.username) && !this.badCredentials) return '';

    if (this.username?.errors?.['required'] && this.hasErrors(this.username)) {
      this.badCredentials = false;
      return 'Username is required';
    }

    if (this.badCredentials) {
      return 'Username and/or password is incorrect';
    }

    return '';
  }

  getPasswordValidationMessages(): string {
    if (!this.hasErrors(this.password)) return '';

    if (this.password?.errors?.['required']) {
      this.badCredentials = false;
      return 'Password is required';
    }

    return '';
  }

  hasErrors(control: AbstractControl | null): boolean {
    if (!control) return false;
    return control.invalid && (control.dirty || control.touched);
  }

  get username() {
    return this.loginFormGroup.get('username');
  }

  get password() {
    return this.loginFormGroup.get('password');
  }
}
