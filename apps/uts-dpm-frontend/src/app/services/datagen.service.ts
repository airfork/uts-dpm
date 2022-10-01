import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpErrorResponse,
  HttpResponse,
} from '@angular/common/http';
import { NotificationService } from './notification.service';
import { environment } from '../../environments/environment';
import { saveAs } from 'file-saver';

const BASE_URL = environment.baseUrl + '/datagen';

@Injectable({
  providedIn: 'root',
})
export class DatagenService {
  constructor(
    private http: HttpClient,
    private notificationService: NotificationService
  ) {}

  downloadUserData() {
    this.http
      .get<Blob>(BASE_URL + '/users', {
        observe: 'response',
        responseType: 'blob' as 'json',
      })
      .subscribe({
        next: (response) => {
          saveAs(
            response.body!,
            this.filenameFromHeaderOrDefault(response, 'Users.xlsx')
          );
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 303) {
            this.notificationService.showWarning(
              'Password change required',
              ''
            );
            return;
          }

          console.error(error);
          this.notificationService.showError(
            'Something went wrong, please try again.',
            'Error'
          );
        },
      });
  }

  downloadDpmData(url: string, callback: () => void) {
    this.http
      .get<Blob>(url, {
        observe: 'response',
        responseType: 'blob' as 'json',
      })
      .subscribe({
        next: (response) => {
          saveAs(
            response.body!,
            this.filenameFromHeaderOrDefault(response, 'DPMs.xlsx')
          );
          callback();
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 303) {
            this.notificationService.showWarning(
              'Password change required',
              ''
            );
            return;
          }

          console.error(error);
          this.notificationService.showError(
            'Something went wrong, please try again.',
            'Error'
          );
        },
      });
  }

  private filenameFromHeaderOrDefault(
    response: HttpResponse<Blob>,
    defaultName: string
  ): string {
    let fileName = defaultName;
    const contentDisposition = response.headers.get('Content-Disposition');

    if (contentDisposition) {
      const fileNameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
      const matches = fileNameRegex.exec(contentDisposition);
      if (matches != null && matches[1]) {
        fileName = matches[1].replace(/['"]/g, '');
      }
    }

    return fileName;
  }
}
