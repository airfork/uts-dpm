import { Injectable } from '@angular/core';
import DPM from '../models/dpm';
import { Observable, of } from 'rxjs';
import DPMType from '../models/dpmType';

interface user {
  id: number;
  dpms: DPM[];
}

const _dpms: DPM[] = [
  {
    id: 1,
    createdBy: 'Tunji',
    driver: 'John Doe',
    block: 'EB',
    date: new Date(),
    type: 'Picked Up Block',
    points: 3,
    startTime: '0600',
    endTime: '1300',
    location: 'OFF',
    created: new Date(),
  },
  {
    id: 2,
    createdBy: 'Tunji',
    driver: 'John Doe',
    block: '[02]',
    date: new Date(),
    type: '6-15 Minutes Late to Blk',
    points: -3,
    startTime: '1500',
    endTime: '1730',
    location: 'OFF',
    notes:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.',
    created: new Date(),
  },
];

const _users: user[] = [{ id: 1, dpms: _dpms.slice(0, 2) }];

export const DPMTypes: DPMType[] = [
  {
    type: 'Type G',
    names: [
      'Picked up Block (+1 Point)',
      'Good! (+1 Point)',
      'Voluntary Clinic/Road Test Passed (+2 Points)',
      '200 Hours Safe (+2 Points)',
      'Custom (+5 Points)',
    ],
  },
  {
    type: 'Type L',
    names: ['1-5 Minutes Late to OFF (-1 Point)'],
  },
  {
    type: 'Type A',
    names: [
      '1-5 Minutes Late to BLK (-1 Point)',
      'Missed Email Announcement (-2 Points)',
      'Improper Shutdown (-2 Points)',
      'Off-Route (-2 Points)',
      '6-15 Minutes Late to Blk (-3 Points)',
      'Out of Uniform (-5 Points)',
      'Improper Radio Procedure (-2 Points)',
      'Improper Bus Log (-5 Points)',
      'Timesheet/Improper Book Change (-5 Points)',
      'Custom (-5 Points)',
    ],
  },
  {
    type: 'Type B',
    names: [
      'Passenger Inconvenience (-5 Points)',
      '16+ Minutes Late (-5 Points)',
      'Attendance Infraction (-10 Points)',
      'Moving Downed Bus (-10 Points)',
      'Improper 10-50 Procedure (-10 Points)',
      'Failed Ride-Along/Road Test (-10 Points)',
      'Custom (-10 Points)',
    ],
  },
  {
    type: 'Type C',
    names: [
      'Failure to Report 10-50 (-15 Points)',
      'Insubordination (-15 Points)',
      'Safety Offense (-15 Points)',
      'Preventable Accident 1, 2 (-15 Points)',
      'Custom (-15 Points)',
    ],
  },
  {
    type: 'Type D',
    names: [
      'DNS/Did Not Show (-10 Points)',
      'Preventable Accident 3, 4 (-20 Points)',
    ],
  },
];

@Injectable({
  providedIn: 'root',
})
export class DpmService {
  private users: user[];

  constructor() {
    this.users = _users;
  }

  findUserById(id: number): Observable<DPM[]> {
    const user = this.users.find((user) => user.id === id);
    if (user) {
      return of(user.dpms);
    }

    return of([]);
  }

  save(dpm: DPM) {
    console.log(dpm);
    this.users[0].dpms.push(dpm);
  }
}
