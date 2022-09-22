import { Injectable } from '@angular/core';
import { catchError, Observable, retry, throwError } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { NotificationService } from './notification.service';
import UsernameDto from '../models/usernameDto';

const BASE_URL = environment.baseUrl + '/users';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(
    private http: HttpClient,
    private notificationService: NotificationService
  ) {}

  getUserNames(): Observable<UsernameDto[]> {
    return this.http
      .get<UsernameDto[]>(`${BASE_URL}/names`)
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
