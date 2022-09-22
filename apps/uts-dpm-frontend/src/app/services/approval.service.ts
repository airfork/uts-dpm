import { Injectable } from '@angular/core';
import { DpmService } from './dpm.service';
import { catchError, Observable, retry, throwError } from 'rxjs';
import ApprovalDpmDto from '../models/approvalDpmDto';
import { HttpClient } from '@angular/common/http';
import { NotificationService } from './notification.service';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ApprovalService {
  private static BASE_URL = environment.baseUrl;

  constructor(
    private dpmService: DpmService,
    private http: HttpClient,
    private notificationService: NotificationService
  ) {}

  getApprovalDpms(): Observable<ApprovalDpmDto[]> {
    return this.http
      .get<ApprovalDpmDto[]>(`${ApprovalService.BASE_URL}/dpms/approvals`)
      .pipe(
        retry(2),
        catchError((error) => {
          this.notificationService.showError('Something went wrong', 'Error');
          return throwError(
            () =>
              new Error(
                'Something went wrong when trying to get the unapproved dpm list'
              )
          );
        })
      );
  }

  updatePoints(id: number, points: number): Observable<any> {
    return this.http
      .patch<any>(`${ApprovalService.BASE_URL}/dpms/${id}`, { points: points })
      .pipe(
        retry(2),
        catchError((error) => {
          this.notificationService.showError('Something went wrong', 'Error');
          return throwError(
            () => new Error('Something went wrong trying to update the points')
          );
        })
      );
  }

  approveDpm(id: number): Observable<any> {
    return this.http
      .patch<any>(`${ApprovalService.BASE_URL}/dpms/${id}`, { approved: true })
      .pipe(
        retry(2),
        catchError((error) => {
          this.notificationService.showError('Something went wrong', 'Error');
          return throwError(
            () => new Error('Something went wrong trying to approve the dpm')
          );
        })
      );
  }

  denyDpm(id: number): Observable<any> {
    return this.http
      .patch<any>(`${ApprovalService.BASE_URL}/dpms/${id}`, { ignored: true })
      .pipe(
        retry(2),
        catchError((error) => {
          this.notificationService.showError('Something went wrong', 'Error');
          return throwError(
            () => new Error('Something went wrong trying to deny the dpm')
          );
        })
      );
  }
}
