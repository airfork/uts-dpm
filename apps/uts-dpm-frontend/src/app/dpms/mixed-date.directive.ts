// start date must be before/equal to end date
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const MixedDateValidator: ValidatorFn = (
  control: AbstractControl
): ValidationErrors | null => {
  const startDate: Date = control.get('startDate')?.value;
  const endDate: Date = control.get('endDate')?.value;

  if (!startDate || !endDate) return null;

  return startDate > endDate ? { mixedDate: true } : null;
};
