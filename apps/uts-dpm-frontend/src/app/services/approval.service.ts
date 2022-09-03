import { Injectable } from '@angular/core';
import { DpmService } from './dpm.service';
import { Observable } from 'rxjs';
import DPM from '../models/dpm';

@Injectable({
  providedIn: 'root',
})
export class ApprovalService {
  constructor(private dpmService: DpmService) {}

  getApprovalDpms(): Observable<DPM[]> {
    return this.dpmService.findUserById(1);
  }
}
