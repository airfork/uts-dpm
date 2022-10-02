import { Component, EventEmitter, Input, Output } from '@angular/core';
import Required from '../../shared/required-decorator';

@Component({
  selector: 'app-confirm-box',
  templateUrl: './confirm-box.component.html',
})
export class ConfirmBoxComponent {
  private _isOpen = false;

  @Output() isOpenChange = new EventEmitter<boolean>();
  @Input() @Required get isOpen(): boolean {
    return this._isOpen;
  }

  set isOpen(value: boolean) {
    this._isOpen = value;
    this.isOpenChange.emit(value);
  }

  @Input() title: string = 'Are you sure?';
  @Input() @Required message = '';
  @Input() @Required outputKey = '';

  @Output() confirmed = new EventEmitter<string>();

  constructor() {}

  confirm() {
    this.confirmed.emit(this.outputKey);
  }
}
