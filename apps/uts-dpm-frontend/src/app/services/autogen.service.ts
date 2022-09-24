import { Injectable } from '@angular/core';
import { DpmService } from './dpm.service';
import DPM from '../models/dpm';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import AutogenDpm from '../models/autogenDpm';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { NotificationService } from './notification.service';

const BASE_URL = environment.baseUrl + '/autogen';

@Injectable({
  providedIn: 'root',
})
export class AutogenService {
  constructor(
    private http: HttpClient,
    private notificationService: NotificationService
  ) {}

  getAutogenDpms(): Observable<AutogenDpm[]> {
    return this.http.get<AutogenDpm[]>(BASE_URL).pipe(
      catchError((error: HttpErrorResponse) => {
        this.notificationService.showError('Something went wrong', 'Error');
        return throwError(
          () => new Error('Something went wrong trying to autogen DPMs')
        );
      })
    );
  }
}
