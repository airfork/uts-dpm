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

  datagenDate(date?: Date): string {
    if (!date) return '';

    return formatDate(date, 'MM-dd-yyyy', this.locale);
  }

  firstname(name: string): string {
    const index = name.indexOf(' ');
    return index === -1 ? name : name.substring(0, index);
  }

  lastname(name: string): string {
    const index = name.indexOf('');
    return index === -1 ? '' : name.substring(index).trim();
  }
}
