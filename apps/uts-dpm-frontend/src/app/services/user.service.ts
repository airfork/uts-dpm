import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

const driverNames = [
  'John Doe',
  'Jane Doe',
  'May Payne',
  'Tim Maxwell',
  'Ruby Rose',
];

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor() {}

  getUsers(): Observable<string[]> {
    return of(driverNames);
  }
}
