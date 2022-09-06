import { Injectable } from '@angular/core';
import { catchError, Observable, of, retry, throwError } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { NotificationService } from './notification.service';

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
  private static BASE_URL = environment.baseUrl;

  constructor(
    private http: HttpClient,
    private notificationService: NotificationService
  ) {}

  getUserNames(): Observable<string[]> {
    return this.http
      .get<string[]>(`${UserService.BASE_URL}/users/names`)
      .pipe(
        retry(2),
        catchError(
          this.handleError('There was an error communicating with the server')
        )
      );
  }

  private handleError(
    toastMessage?: string
  ): (error: HttpErrorResponse) => Observable<never> {
    return (error: HttpErrorResponse) => {
      if (error.status === 0) {
        // A client-side or network error occurred. Handle it accordingly.
        console.error('An error occurred:', error.error);
      } else {
        // The backend returned an unsuccessful response code.
        // The response body may contain clues as to what went wrong.
        console.error(
          `Backend returned code ${error.status}, body was: `,
          error.error
        );

        if (toastMessage) {
          this.notificationService.showError(toastMessage, 'Error');
        }
      }
      // Return an observable with a user-facing error message.
      return throwError(
        () => new Error('Something bad happened; please try again later.')
      );
    };
  }
}
