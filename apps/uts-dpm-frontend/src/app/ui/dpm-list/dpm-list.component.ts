import { Component, Input, OnInit } from '@angular/core';
import DPM from '../../models/dpm';

@Component({
  selector: 'app-dpm-list[dpms]',
  templateUrl: './dpm-list.component.html',
  styleUrls: ['./dpm-list.component.scss'],
})
export class DpmListComponent implements OnInit {
  @Input()
  dpms?: DPM[];

  @Input()
  dpmClick?: (dpm: DPM) => void;

  constructor() {}

  ngOnInit() {
    if (!this.dpms) {
      throw new TypeError('dpms is required');
    }
  }
}
