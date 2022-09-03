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

  points(points?: number): string {
    if (!points) return '';

    if (points > 0) {
      return `+${points}`;
    }
    return points.toString();
  }

  created(date?: Date): string {
    if (!date) return '';

    return formatDate(date, 'MM/dd/yyyy HH:mm', this.locale);
  }
}
