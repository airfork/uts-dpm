import { Injectable } from '@angular/core';
import { catchError, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import AutogenWrapper from '../models/autogen-wrapper';
import { ErrorService } from './error.service';

const BASE_URL = environment.baseUrl + '/autogen';

@Injectable({
  providedIn: 'root',
})
export class AutogenService {
  constructor(private http: HttpClient, private errorService: ErrorService) {}

  getAutogenDpms(): Observable<AutogenWrapper> {
    return this.http.get<AutogenWrapper>(BASE_URL).pipe(
      catchError((error: HttpErrorResponse) => {
        return this.errorService.errorResponse(
          error,
          'Something went wrong trying to autogen the DPMs'
        );
      })
    );
  }

  submit(): Observable<any> {
    return this.http.post(BASE_URL + '/submit', null).pipe(
      catchError((error: HttpErrorResponse) => {
        return this.errorService.errorResponse(
          error,
          'Something went wrong trying to submit the autogen DPMs'
        );
      })
    );
  }
}
