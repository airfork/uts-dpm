import { Component } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MixedDateValidator } from '../../directives/mixed-date.directive';
import { FormatService } from '../../services/format.service';

type startEndDate = 'Start Date' | 'End Date';

@Component({
  selector: 'app-datagen',
  templateUrl: './datagen.component.html',
  styleUrls: ['./datagen.component.scss'],
})
export class DatagenComponent {
  dpmDataFormGroup = new FormGroup(
    {
      startDate: new FormControl(null, [Validators.required]),
      endDate: new FormControl(null, [Validators.required]),
      getAll: new FormControl(false, { nonNullable: true }),
    },
    { validators: MixedDateValidator }
  );
  constructor(private formatService: FormatService) {}

  errorsOrEmpty(control: AbstractControl | null): string {
    return this.hasErrors(control) ||
      this.dpmDataFormGroup.errors?.['mixedDate']
      ? 'input-error'
      : '';
  }

  hasErrors(control: AbstractControl<any> | null): boolean {
    if (!control) return false;
    return control.invalid && (control.dirty || control.touched);
  }

  getStartTimeValidationMessages(): string {
    if (!this.hasErrors(this.startDate)) {
      if (this.dpmDataFormGroup.errors?.['mixedDate']) {
        return 'Start date cannot be after end date';
      }

      return '';
    }

    return this.getDateValidationMessages('Start Date', this.startDate?.errors);
  }

  getEndTimeValidationMessages(): string {
    if (!this.hasErrors(this.endDate)) {
      if (this.dpmDataFormGroup.errors?.['mixedDate']) {
        return 'End date cannot be before start date';
      }

      return '';
    }

    return this.getDateValidationMessages('End Date', this.endDate?.errors);
  }

  onFormSubmit() {
    console.log(this.dpmDataFormGroup.value);
    this.dpmDataFormGroup.reset();
  }

  onUserSubmit() {
    console.log('Generating user data');
  }

  get startDate() {
    return this.dpmDataFormGroup.get('startDate');
  }

  get endDate() {
    return this.dpmDataFormGroup.get('endDate');
  }

  get getAll() {
    return this.dpmDataFormGroup.get('getAll');
  }

  get format() {
    return this.formatService;
  }

  private getDateValidationMessages(
    title: startEndDate,
    errors: ValidationErrors | null | undefined
  ) {
    if (errors?.['required']) {
      return `${title} is required`;
    }

    return 'Unknown validation error';
  }
}
