import { Injectable } from '@angular/core';
import jwtDecode from 'jwt-decode';
import TokenPayload from '../models/TokenPayload';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import UserData from '../models/userData';

const BASE_URL = environment.baseUrl + '/auth';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  userData: UserData;

  constructor(private http: HttpClient) {
    this.userData = new UserData();
    this.setUserData();
  }

  login(username: string, password: string): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(BASE_URL + '/login', {
      username,
      password,
    });
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('exp');
    localStorage.removeItem('username');
    this.userData.clear();
  }

  saveToken(token: string) {
    const payload = jwtDecode(token) as TokenPayload;
    localStorage.setItem('token', token);
    localStorage.setItem('role', payload.role);
    localStorage.setItem('exp', String(payload.exp));
    localStorage.setItem('username', payload.sub);
    this.setUserData();
  }

  isAuthenticated(): boolean {
    const now = Math.floor(new Date().getTime() / 1000);
    return now < this.userData.exp && this.userData.token != '';
  }

  private setUserData() {
    this.userData.token = localStorage.getItem('token') || '';
    this.userData.role = localStorage.getItem('role') || '';
    this.userData.username = localStorage.getItem('username') || '';
    const exp = localStorage.getItem('exp');
    if (exp) {
      this.userData.exp = +exp;
    }
  }
}
