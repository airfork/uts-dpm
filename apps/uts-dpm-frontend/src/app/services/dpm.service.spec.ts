import { TestBed } from '@angular/core/testing';

import { DpmService } from './dpm.service';

describe('ApiService', () => {
  let service: DpmService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DpmService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
