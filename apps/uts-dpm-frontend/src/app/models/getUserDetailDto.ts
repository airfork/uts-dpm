import UserDetailDto from './userDetailDto';

export default interface GetUserDetailDto extends UserDetailDto {
  managers: string[];
}
