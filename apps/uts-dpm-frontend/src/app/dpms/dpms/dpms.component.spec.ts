import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DpmsComponent } from './dpms.component';

describe('DpmsComponent', () => {
  let component: DpmsComponent;
  let fixture: ComponentFixture<DpmsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DpmsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DpmsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
