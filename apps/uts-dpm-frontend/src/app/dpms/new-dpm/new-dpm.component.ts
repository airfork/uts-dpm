import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { DpmService, DPMTypes } from '../../services/dpm.service';
import { UserService } from '../../services/user.service';
import { NotificationService } from '../../services/notification.service';
import { FormatService } from '../../services/format.service';
import PostDpmDto from '../../models/postDpmDto';
import UsernameDto from '../../models/usernameDto';
import { first } from 'rxjs';

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
  changeDetection: ChangeDetectionStrategy.OnPush,
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

  private driverNames: UsernameDto[] = [];

  autocompleteResults: string[] = [];

  constructor(
    private userService: UserService,
    private dpmService: DpmService,
    private notificationService: NotificationService,
    private formatService: FormatService
  ) {}

  ngOnInit() {
    this.userService
      .getUserNames()
      .pipe(first())
      .subscribe((users) => (this.driverNames = users));
  }

  search(event: queryResult) {
    this.autocompleteResults = this.driverNames
      .filter((user) =>
        user.name.toLowerCase().includes(event.query.toLowerCase())
      )
      .map((user) => user.name);
  }

  errorsOrSuccess(control: AbstractControl | null): string {
    return this.hasErrors(control) ? 'input-error' : 'input-success';
  }

  setStatusClass(control: AbstractControl | null, isInput = true): string {
    if (control == null) return '';
    const prefix = isInput ? 'input-' : 'text-';

    if (this.hasErrors(control)) return prefix + 'error';
    if (control.dirty || control.touched) return prefix + 'success';
    return '';
  }

  onSubmit() {
    this.dpmService
      .create(this.formGroupToDto())
      .pipe(first())
      .subscribe(() => {
        this.notificationService.showSuccess('DPM Created', 'Success');
        this.homeFormGroup.reset({
          dpmDate: new Date(),
          type: this.defaultDpmType,
        });
      });
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

  hasErrors(control: AbstractControl | null): boolean {
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

  private formGroupToDto(): PostDpmDto {
    const values = this.homeFormGroup.value;
    const dto: PostDpmDto = {
      driver: values.name!,
      block: values.block!,
      date: this.formatService.dpmDate(values.dpmDate!),
      type: values.type!,
      location: values.location!,
      startTime: values.startTime!,
      endTime: values.endTime!,
    };

    if (values.notes) {
      dto.notes = values.notes;
    }

    return dto;
  }
}
