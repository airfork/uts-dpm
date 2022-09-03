import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { DpmService, DPMTypes } from '../../services/dpm.service';
import { UserService } from '../../services/user.service';
import DPM from '../../models/dpm';
import { NotificationService } from '../../services/notification.service';
import { FormatService } from '../../services/format.service';

type startEndTime = 'Start Time' | 'End Time';
const regex24HourTime = /^(?:[01][0-9]|2[0-3])[0-5][0-9](?::[0-5][0-9])?$/;

interface queryResult {
  originalEvent: InputEvent;
  query: string;
}

@Component({
  selector: 'app-new-dpm',
  templateUrl: './new-dpm.component.html',
  styleUrls: ['./new-dpm.component.scss'],
})
export class NewDpmComponent implements OnInit {
  dpmTypes = DPMTypes;
  private defaultDpmType = this.dpmTypes[0].names[0];

  homeFormGroup = new FormGroup({
    dpmDate: new FormControl(new Date(), [Validators.required]),
    startTime: new FormControl('', [
      Validators.required,
      Validators.maxLength(4),
      Validators.minLength(4),
      Validators.pattern(regex24HourTime),
    ]),
    endTime: new FormControl('', [
      Validators.required,
      Validators.maxLength(4),
      Validators.minLength(4),
      Validators.pattern(regex24HourTime),
    ]),
    name: new FormControl('', [Validators.required]),
    block: new FormControl('', [Validators.required, Validators.maxLength(5)]),
    location: new FormControl('', [
      Validators.required,
      Validators.maxLength(5),
    ]),
    type: new FormControl(this.defaultDpmType),
    notes: new FormControl(''),
  });

  driverNames: string[] = [];

  autocompleteResults: string[] = [];

  constructor(
    private userService: UserService,
    private dpmService: DpmService,
    private notificationService: NotificationService,
    private formatService: FormatService
  ) {}

  ngOnInit() {
    this.userService
      .getUsers()
      .subscribe((users) => (this.driverNames = users));
  }

  search(event: queryResult) {
    this.autocompleteResults = this.driverNames.filter((value) =>
      value.toLowerCase().includes(event.query.toLowerCase())
    );
  }

  errorsOrEmpty(control: AbstractControl | null): string {
    return this.hasErrors(control) ? 'input-error' : '';
  }

  onSubmit() {
    this.dpmService.save(this.homeFormGroup.value as DPM);
    setTimeout(
      () =>
        this.homeFormGroup.reset({
          dpmDate: new Date(),
          type: this.defaultDpmType,
        }),
      1000
    );

    this.notificationService.showSuccess('DPM Created', '');
  }

  getStartTimeValidationMessages(): string {
    if (!this.hasErrors(this.startTime)) return '';

    return this.getTimeValidationMessages(
      'Start Time',
      this.startTime?.errors,
      this.startTime?.value?.toString()
    );
  }

  getEndTimeValidationMessages(): string {
    if (!this.hasErrors(this.endTime)) return '';

    return this.getTimeValidationMessages(
      'End Time',
      this.endTime?.errors,
      this.endTime?.value?.toString()
    );
  }

  getDpmDateValidationMessages(): string {
    if (!this.hasErrors(this.dpmDate)) return '';

    if (this.dpmDate?.errors?.['required']) {
      return 'Date is required';
    }

    return '';
  }

  getNameValidationMessages(): string {
    if (!this.hasErrors(this.name)) return '';

    if (this.name?.errors?.['required']) {
      return 'Name is required';
    }

    return '';
  }

  getBlockValidationMessages(): string {
    if (!this.hasErrors(this.block)) return '';

    if (this.name?.errors?.['required']) {
      return 'Block is required';
    }

    if (this.name?.errors?.['maxlength']) {
      return 'Block cannot be longer than 5 characters';
    }

    return '';
  }

  getLocationValidationMessages(): string {
    if (!this.hasErrors(this.location)) return '';

    if (this.name?.errors?.['required']) {
      return 'Location is required';
    }

    if (this.name?.errors?.['maxlength']) {
      return 'Location cannot be longer than 5 characters';
    }

    return '';
  }

  hasErrors(control: AbstractControl<any> | null): boolean {
    if (!control) return false;
    return control.invalid && (control.dirty || control.touched);
  }

  get dpmDate() {
    return this.homeFormGroup.get('dpmDate');
  }

  get startTime() {
    return this.homeFormGroup.get('startTime');
  }

  get endTime() {
    return this.homeFormGroup.get('endTime');
  }

  get name() {
    return this.homeFormGroup.get('name');
  }

  get block() {
    return this.homeFormGroup.get('block');
  }

  get location() {
    return this.homeFormGroup.get('location');
  }

  get format() {
    return this.formatService;
  }

  private getTimeValidationMessages(
    title: startEndTime,
    errors: ValidationErrors | null | undefined,
    value: String | undefined
  ): string {
    if (errors?.['required']) {
      return `${title} is required`;
    }

    if (errors?.['minlength'] || errors?.['maxlength']) {
      return `${title} must be 4 digits`;
    }

    if (errors?.['pattern']) {
      if (value) {
        return `'${value}' is not a valid time`;
      }
      return 'Invalid time';
    }

    return 'Unknown validation error';
  }
}
