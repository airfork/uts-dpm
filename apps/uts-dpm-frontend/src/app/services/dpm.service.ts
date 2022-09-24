import { Injectable } from '@angular/core';
import DPM from '../models/dpm';
import { catchError, Observable, of, retry, throwError } from 'rxjs';
import PostDpmDto from '../models/postDpmDto';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { NotificationService } from './notification.service';
import { environment } from '../../environments/environment';
import HomeDpmDto from '../models/homeDpmDto';
import DpmDetailPage from '../models/dpmDetailPage';

const BASE_URL = environment.baseUrl + '/dpms';

@Injectable({
  providedIn: 'root',
})
export class DpmService {
  constructor(
    private http: HttpClient,
    private notificationService: NotificationService
  ) {}

  getCurrentDpms(): Observable<HomeDpmDto[]> {
    return this.http.get<HomeDpmDto[]>(BASE_URL + '/current').pipe(
      retry(2),
      catchError((error: HttpErrorResponse) => {
        this.notificationService.showError('Something went wrong', 'Error');
        return throwError(
          () =>
            new Error(
              "Something went wrong trying to get the user's current dpms"
            )
        );
      })
    );
  }

  create(dpm: PostDpmDto): Observable<any> {
    console.log(dpm);
    return this.http.post(BASE_URL, dpm).pipe(
      catchError((error) => {
        this.notificationService.showError('Failed to create DPM', 'Error');
        return throwError(
          () => new Error('Something went wrong trying to create the DPM')
        );
      })
    );
  }

  getAll(id: string, page: number, size: number): Observable<DpmDetailPage> {
    return this.http
      .get<DpmDetailPage>(`${BASE_URL}/user/${id}?page=${page}&size=${size}`)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          this.notificationService.showError('Something went wrong', 'Error');
          return throwError(
            () =>
              new Error("Something went wrong trying to get the user's dpms")
          );
        })
      );
  }
}
