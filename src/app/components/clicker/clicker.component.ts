import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-clicker',
  templateUrl: './clicker.component.html',
  styleUrls: ['./clicker.component.scss'],
})
export class ClickerComponent {
  clicks: number;

  constructor() {
    this.clicks = 0;
  }

  onButtonClick = () => this.clicks++;
}
