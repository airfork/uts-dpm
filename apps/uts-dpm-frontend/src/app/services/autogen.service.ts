import { Injectable } from '@angular/core';
import { DpmService } from './dpm.service';
import DPM from '../models/dpm';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AutogenService {
  constructor(private dpmService: DpmService) {}

  getAutogenDpms(): Observable<DPM[]> {
    return this.dpmService.findUserById(1);
  }
}
