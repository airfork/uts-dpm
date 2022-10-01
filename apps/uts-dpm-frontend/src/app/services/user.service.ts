import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { NotificationService } from './notification.service';
import UsernameDto from '../models/username-dto';
import { Router } from '@angular/router';
import GetUserDetailDto from '../models/get-user-detail-dto';
import UserDetailDto from '../models/user-detail-dto';
import CreateUserDto from '../models/create-user-dto';
import { ErrorService } from './error.service';

const BASE_URL = environment.baseUrl + '/users';
const ROLES = ['Admin', 'Analyst', 'Driver', 'Manager', 'Supervisor'];

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(
    private http: HttpClient,
    private notificationService: NotificationService,
    private router: Router,
    private errorService: ErrorService
  ) {}

  getUserNames(): Observable<UsernameDto[]> {
    return this.http.get<UsernameDto[]>(`${BASE_URL}/names`).pipe(
      catchError((error: HttpErrorResponse) => {
        return this.errorService.errorResponse(
          error,
          'Something went wrong trying to get the list of user names'
        );
      })
    );
  }

  getUser(id: string): Observable<GetUserDetailDto> {
    return this.http.get<GetUserDetailDto>(`${BASE_URL}/${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 303) {
          this.notificationService.showWarning('Password change required', '');
          return throwError(
            () => new Error('Request failed, password change required')
          );
        }

        if (error.status === 404) {
          this.router.navigate(['/errors/404']);
          return throwError(
            () => new Error('Failed to find user with id: ' + id)
          );
        }

        console.error(error);
        this.notificationService.showError(
          'Something went wrong, please try again.',
          'Error'
        );
        return throwError(
          () =>
            new Error(
              'Something went wrong trying to find the user with id: ' + id
            )
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
        return this.errorService.errorResponse(
          error,
          'Something went wrong trying to update the user'
        );
      })
    );
  }

  getManagers(): Observable<string[]> {
    return this.http.get<string[]>(BASE_URL + '/managers').pipe(
      catchError((error: HttpErrorResponse) => {
        return this.errorService.errorResponse(
          error,
          'Something went wrong trying to get the managers list'
        );
      })
    );
  }

  createUser(dto: CreateUserDto): Observable<any> {
    return this.http.post(BASE_URL, dto).pipe(
      catchError((error: HttpErrorResponse) => {
        return this.errorService.errorResponse(
          error,
          'Something went wrong trying to create a user'
        );
      })
    );
  }
}
