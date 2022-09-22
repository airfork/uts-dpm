import { Inject, Injectable, LOCALE_ID } from '@angular/core';
import { formatDate } from '@angular/common';

@Injectable({
  providedIn: 'root',
})
export class FormatService {
  constructor(@Inject(LOCALE_ID) private locale: string) {}

  dpmDate(date?: Date): string {
    if (!date) {
      date = new Date();
    }

    return formatDate(date, 'MM/dd/yyyy', this.locale);
  }

  datagenDate(date?: Date): string {
    if (!date) return '';

    return formatDate(date, 'MM-dd-yyyy', this.locale);
  }
}
