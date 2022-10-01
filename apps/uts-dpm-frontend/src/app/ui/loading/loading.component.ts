import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading',
  templateUrl: './loading.component.html',
  styleUrls: ['./loading.component.scss'],
})
export class LoadingComponent {
  public _halfScreen: boolean = false;

  @Input()
  get halfScreen() {
    return this._halfScreen;
  }

  set halfScreen(value: any) {
    this._halfScreen = this.coerceBooleanProperty(value);
  }

  coerceBooleanProperty(value: any): boolean {
    return value != null && `${value}` !== 'false';
  }

  constructor() {}
}
