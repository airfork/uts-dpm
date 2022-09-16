import { Component } from '@angular/core';
import { AbstractControl, FormControl, FormGroup } from '@angular/forms';
import { MixedDateValidator } from '../../directives/mixed-date.directive';
import { FormatService } from '../../services/format.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-datagen',
  templateUrl: './datagen.component.html',
  styleUrls: ['./datagen.component.scss'],
})
export class DatagenComponent {
  private BASE_URL = environment.baseUrl + '/datagen';
  USERS_URL = environment.baseUrl + '/datagen/users';

  dpmDataFormGroup = new FormGroup(
    {
      startDate: new FormControl<Date | null>(null),
      endDate: new FormControl(new Date()),
      getAll: new FormControl(false, { nonNullable: true }),
    },
    { validators: MixedDateValidator }
  );

  constructor(private formatService: FormatService) {}

  errorsOrEmpty(control: AbstractControl | null): string {
    if (this.dpmDataFormGroup.errors?.['mixedDate'] && !this.getAll?.value) {
      return 'input-error';
    }

    return '';
  }

  getStartTimeValidationMessages(): string {
    if (this.getAll?.value) return '';

    if (this.dpmDataFormGroup.errors?.['mixedDate']) {
      return 'Start date cannot be after end date';
    }

    return '';
  }

  getEndTimeValidationMessages(): string {
    if (this.getAll?.value) return '';

    if (this.dpmDataFormGroup.errors?.['mixedDate']) {
      return 'End date cannot be before start date';
    }

    return '';
  }

  onFormSubmit() {
    // need to wait a bit so that generateDownloadLink function
    // generates a proper link instead of '#'
    setTimeout(() => this.dpmDataFormGroup.reset(), 500);
  }

  onUserSubmit() {
    console.log('Generating user data');
  }

  generateDownloadLink(): string {
    if (this.dpmDataFormGroup.invalid && !this.dpmDataFormGroup.value.getAll)
      return '#';

    const values = this.dpmDataFormGroup.value;

    if (values.getAll) {
      return `${this.BASE_URL}/dpms`;
    }

    let startDateParam = '';
    let endDateParam = '';

    if (values.startDate) {
      startDateParam = `?startDate=${this.format.datagenDate(
        values.startDate
      )}`;
    }

    if (values.endDate) {
      const prefix = values.startDate ? '&' : '?';
      endDateParam = `${prefix}endDate=${this.format.datagenDate(
        values.endDate
      )}`;
    }

    return `${this.BASE_URL}/dpms${startDateParam}${endDateParam}`;
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
}
