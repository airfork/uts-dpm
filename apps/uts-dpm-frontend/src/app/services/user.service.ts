import { Injectable } from '@angular/core';
import { catchError, Observable, retry, throwError } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { NotificationService } from './notification.service';
import UsernameDto from '../models/usernameDto';
import { Router } from '@angular/router';
import GetUserDetailDto from '../models/getUserDetailDto';
import UserDetailDto from '../models/userDetailDto';
import CreateUserDto from '../models/createUserDto';

const BASE_URL = environment.baseUrl + '/users';
const ROLES = ['Admin', 'Analyst', 'Driver', 'Manager', 'Supervisor'];

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(
    private http: HttpClient,
    private notificationService: NotificationService,
    private router: Router
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

  getUser(id: string): Observable<GetUserDetailDto> {
    return this.http.get<GetUserDetailDto>(`${BASE_URL}/${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          this.router.navigate(['/errors/404']);
        } else {
          this.notificationService.showError('Something went wrong', 'Error');
        }
        return throwError(
          () => new Error('Something went wrong trying to find the user')
        );
      })
    );
  }

  orderRoles(currentRole: string): string[] {
    if (!ROLES.includes(currentRole)) {
      console.warn(`Failed to find role '${currentRole}' in roles list`);
      return ROLES;
    }

    const filteredRoles = [currentRole];
    filteredRoles.push(...ROLES.filter((value) => value !== currentRole));
    return filteredRoles;
  }

  orderManagers(currentManager: string, managers: string[]): string[] {
    if (!managers.includes(currentManager)) {
      console.warn(
        `Failed to find manager '${currentManager}' in manager list`
      );
      return managers;
    }

    const filteredManagers = [currentManager];
    filteredManagers.push(
      ...managers.filter((value) => value !== currentManager)
    );
    return filteredManagers;
  }

  updateUser(dto: UserDetailDto, id: string): Observable<any> {
    return this.http.patch(`${BASE_URL}/${id}`, dto).pipe(
      catchError((error: HttpErrorResponse) => {
        this.notificationService.showError('Failed to update user', 'Error');
        return throwError(
          () => new Error('Something went wrong trying to update the user')
        );
      })
    );
  }

  getManagers(): Observable<string[]> {
    return this.http.get<string[]>(BASE_URL + '/managers').pipe(
      catchError((error: HttpErrorResponse) => {
        this.notificationService.showError(
          'Something went wrong, please try again.',
          'Error'
        );
        return throwError(
          () =>
            new Error('Something went wrong trying to get the managers list')
        );
      })
    );
  }

  createUser(dto: CreateUserDto): Observable<any> {
    return this.http.post(BASE_URL, dto);
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
