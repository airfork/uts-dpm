import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-error-page',
  templateUrl: './error-page.component.html',
})
export class ErrorPageComponent {
  @Input() errorCode: string = '';
  @Input() message: string = '';

  constructor() {}
}
