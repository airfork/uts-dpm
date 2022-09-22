import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { NotificationService } from './notification.service';
import { catchError, Observable, throwError } from 'rxjs';
import GetUserDetailDto from '../models/getUserDetailDto';
import { Router } from '@angular/router';
import UserDetailDto from '../models/userDetailDto';

const BASE_URL = environment.baseUrl + '/users';
const ROLES = ['Admin', 'Analyst', 'Driver', 'Manager', 'Supervisor'];

@Injectable({
  providedIn: 'root',
})
export class UserDetailService {
  constructor(
    private http: HttpClient,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  getUser(id: string): Observable<GetUserDetailDto> {
    return this.http.get<GetUserDetailDto>(`${BASE_URL}/${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          this.router.navigate(['/error/404']);
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
}
