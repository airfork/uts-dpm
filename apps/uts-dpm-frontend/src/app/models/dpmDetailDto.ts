import ApprovalDpmDto from './approvalDpmDto';

export default interface DpmDetailDto extends ApprovalDpmDto {
  status: string;
  ignored: boolean;
}
