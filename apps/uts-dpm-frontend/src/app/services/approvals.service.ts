import { Injectable } from '@angular/core';
import { DpmService } from './dpm.service';
import { catchError, Observable, retry } from 'rxjs';
import ApprovalDpmDto from '../models/approval-dpm-dto';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { ErrorService } from './error.service';

const BASE_URL = environment.baseUrl + '/dpms';

@Injectable({
  providedIn: 'root',
})
export class ApprovalsService {
  constructor(
    private dpmService: DpmService,
    private http: HttpClient,
    private errorService: ErrorService
  ) {}

  getApprovalDpms(): Observable<ApprovalDpmDto[]> {
    return this.http.get<ApprovalDpmDto[]>(BASE_URL + '/approvals').pipe(
      catchError((error: HttpErrorResponse) => {
        return this.errorService.errorResponse(
          error,
          'Something went wrong when trying to get the unapproved dpm list'
        );
      })
    );
  }

  updatePoints(id: number, points: number): Observable<any> {
    return this.http
      .patch<any>(`${BASE_URL}/dpms/${id}`, { points: points })
      .pipe(
        catchError((error) => {
          return this.errorService.errorResponse(
            error,
            "Something went wrong when trying to update the user's points"
          );
        })
      );
  }

  approveDpm(id: number): Observable<any> {
    return this.http
      .patch<any>(`${BASE_URL}/dpms/${id}`, { approved: true })
      .pipe(
        retry(2),
        catchError((error) => {
          return this.errorService.errorResponse(
            error,
            'Something went wrong trying to approve the dpm'
          );
        })
      );
  }

  denyDpm(id: number): Observable<any> {
    return this.http
      .patch<any>(`${BASE_URL}/dpms/${id}`, { ignored: true })
      .pipe(
        catchError((error) => {
          return this.errorService.errorResponse(
            error,
            'Something went wrong trying to deny the dpm'
          );
        })
      );
  }
}
