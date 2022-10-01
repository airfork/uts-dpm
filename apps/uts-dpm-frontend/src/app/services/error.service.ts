import { Injectable } from '@angular/core';
import { NotificationService } from './notification.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ErrorService {
  constructor(private notificationService: NotificationService) {}

  errorResponse(
    error: HttpErrorResponse,
    detailMessage: string
  ): Observable<never> {
    if (error.status === 303) {
      this.notificationService.showWarning('Password change required', '');
      return throwError(
        () => new Error('Request failed, password change required')
      );
    }

    this.notificationService.showError(
      'Something went wrong, please try again.',
      'Error'
    );
    console.error(error);
    return throwError(() => new Error(detailMessage));
  }
}
