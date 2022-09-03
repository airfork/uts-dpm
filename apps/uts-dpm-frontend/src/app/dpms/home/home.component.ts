import { Component, OnInit } from '@angular/core';
import DPM from '../../models/dpm';
import { DpmService } from '../../services/dpm.service';
import { FormatService } from '../../services/format.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  dpms: DPM[] = [];
  modalOpen = false;
  currentDpm: DPM | null = null;

  constructor(
    private apiService: DpmService,
    private formatService: FormatService
  ) {}

  ngOnInit(): void {
    this.getDpms();
  }

  clickRow(dpm: DPM): void {
    this.modalOpen = true;
    this.currentDpm = dpm;
  }

  get format() {
    return this.formatService;
  }

  private getDpms(): void {
    this.apiService.findUserById(1).subscribe((values) => (this.dpms = values));
  }
}
