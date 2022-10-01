// Make sure that current password and new password are equal to each other
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const PasswordsNotEqualValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const currentPassword: string = control.get('currentPassword')?.value;
  const newPassword: string = control.get('newPassword')?.value;

  if (!currentPassword || !newPassword) return null;
  if (newPassword.length < 8) return null;

  return currentPassword === newPassword ? { passwordsNotEqual: true } : null;
};
