import { ChangeDetectionStrategy, Component } from '@angular/core';
import { DpmService } from '../../services/dpm.service';
import { FormatService } from '../../services/format.service';
import HomeDpmDto from '../../models/homeDpmDto';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent {
  currentDpms$: Observable<HomeDpmDto[]>;
  modalOpen = false;
  currentDpm?: HomeDpmDto;

  constructor(
    private dpmService: DpmService,
    private formatService: FormatService
  ) {
    this.currentDpms$ = this.dpmService.getCurrentDpms();
  }

  clickRow(dpm: HomeDpmDto) {
    this.modalOpen = true;
    this.currentDpm = dpm;
  }

  get format() {
    return this.formatService;
  }
}
