import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { NotificationService } from './notification.service';
import AutogenWrapper from '../models/autogenWrapper';

const BASE_URL = environment.baseUrl + '/autogen';

@Injectable({
  providedIn: 'root',
})
export class AutogenService {
  constructor(
    private http: HttpClient,
    private notificationService: NotificationService
  ) {}

  getAutogenDpms(): Observable<AutogenWrapper> {
    return this.http.get<AutogenWrapper>(BASE_URL).pipe(
      catchError((error: HttpErrorResponse) => {
        this.notificationService.showError('Something went wrong', 'Error');
        return throwError(
          () => new Error('Something went wrong trying to autogen DPMs')
        );
      })
    );
  }

  submit(): Observable<any> {
    return this.http.post(BASE_URL + '/submit', null).pipe(
      catchError((error: HttpErrorResponse) => {
        this.notificationService.showError('Something went wrong', 'Error');
        return throwError(
          () => new Error('Something went wrong trying to submit autogen DPMs')
        );
      })
    );
  }
}
