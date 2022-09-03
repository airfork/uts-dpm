import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewDpmComponent } from './new-dpm.component';

describe('DpmComponent', () => {
  let component: NewDpmComponent;
  let fixture: ComponentFixture<NewDpmComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NewDpmComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(NewDpmComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
