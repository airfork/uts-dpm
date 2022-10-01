import ApprovalDpmDto from './approval-dpm-dto';

export default interface DpmDetailDto extends ApprovalDpmDto {
  status: string;
  ignored: boolean;
}
