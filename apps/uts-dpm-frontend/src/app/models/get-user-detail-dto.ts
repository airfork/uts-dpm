import UserDetailDto from './user-detail-dto';

export default interface GetUserDetailDto extends UserDetailDto {
  managers: string[];
}
