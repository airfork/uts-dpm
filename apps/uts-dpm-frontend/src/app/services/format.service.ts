import { Inject, Injectable, LOCALE_ID } from '@angular/core';
import { formatDate } from '@angular/common';

@Injectable({
  providedIn: 'root',
})
export class FormatService {
  constructor(@Inject(LOCALE_ID) private locale: string) {}

  dpmDate(date?: Date | null, subtractMonth: boolean = false): string {
    if (!date) {
      date = new Date();
    }

    if (subtractMonth) {
      date.setMonth(date.getMonth() - 1);
    }

    return formatDate(date, 'MM/dd/yyyy', this.locale);
  }

  datagenDate(date?: Date): string {
    if (!date) return '';

    return formatDate(date, 'MM-dd-yyyy', this.locale);
  }
}
