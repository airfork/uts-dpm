export interface DPMType {
  type: string;
  names: string[];
}

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
