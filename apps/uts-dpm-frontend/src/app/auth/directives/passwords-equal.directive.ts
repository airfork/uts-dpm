// Make sure that new password and confirm password are equal to each other
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const PasswordsEqualValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const newPassword: string = control.get('newPassword')?.value;
  const confirmPassword: string = control.get('confirmPassword')?.value;

  if (!newPassword || !confirmPassword) return null;
  if (newPassword.length < 8) return null;

  return newPassword !== confirmPassword ? { passwordsEqual: true } : null;
};
