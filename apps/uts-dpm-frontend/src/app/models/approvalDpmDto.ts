export default interface ApprovalDpmDto {
  id: number;
  driver: string;
  createdBy: string;
  type: string;
  points: number;
  block: string;
  location: string;
  date: string;
  time: string;
  createdAt: string;
  notes?: string;
}
