import { Injectable } from '@angular/core';
import { catchError, Observable, retry } from 'rxjs';
import PostDpmDto from '../models/post-dpm-dto';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import HomeDpmDto from '../models/home-dpm-dto';
import DpmDetailPage from '../models/dpm-detail-page';
import { ErrorService } from './error.service';

const BASE_URL = environment.baseUrl + '/dpms';

@Injectable({
  providedIn: 'root',
})
export class DpmService {
  constructor(private http: HttpClient, private errorService: ErrorService) {}

  getCurrentDpms(): Observable<HomeDpmDto[]> {
    return this.http.get<HomeDpmDto[]>(BASE_URL + '/current').pipe(
      retry(2),
      catchError((error: HttpErrorResponse) => {
        return this.errorService.errorResponse(
          error,
          "Something went wrong trying to get the user's current dpms"
        );
      })
    );
  }

  create(dpm: PostDpmDto): Observable<any> {
    console.log(dpm);
    return this.http.post(BASE_URL, dpm).pipe(
      catchError((error) => {
        return this.errorService.errorResponse(
          error,
          'Something went wrong trying to create the DPM'
        );
      })
    );
  }

  getAll(id: string, page: number, size: number): Observable<DpmDetailPage> {
    return this.http
      .get<DpmDetailPage>(`${BASE_URL}/user/${id}?page=${page}&size=${size}`)
      .pipe(
        catchError((error: HttpErrorResponse) => {
          return this.errorService.errorResponse(
            error,
            "Something went wrong trying to get the user' dpms"
          );
        })
      );
  }
}
