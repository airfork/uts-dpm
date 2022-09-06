import { Component, OnInit } from '@angular/core';
import { DpmService } from '../../services/dpm.service';
import { FormatService } from '../../services/format.service';
import HomeDpmDto from '../../models/homeDpmDto';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  dpms: HomeDpmDto[] = [];
  modalOpen = false;
  currentDpm?: HomeDpmDto;

  constructor(
    private apiService: DpmService,
    private formatService: FormatService
  ) {}

  ngOnInit(): void {
    this.getDpms();
  }

  clickRow(dpm: HomeDpmDto): void {
    this.modalOpen = true;
    this.currentDpm = dpm;
  }

  get format() {
    return this.formatService;
  }

  private getDpms() {
    this.apiService
      .getCurrentDpms()
      .subscribe((values) => (this.dpms = values));
  }
}
