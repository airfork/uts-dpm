import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DpmListComponent } from './dpm-list.component';

describe('DpmListComponent', () => {
  let component: DpmListComponent;
  let fixture: ComponentFixture<DpmListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DpmListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DpmListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
