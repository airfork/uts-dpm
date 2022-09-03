export default interface DPM {
  driver: string;
  block: string;
  date: Date;
  type: string;
  location: string;
  startTime: string;
  endTime: string;
  id?: number;
  createdBy?: string;
  points?: number;
  notes?: string;
  created?: Date;
  approved?: boolean;
}
