export default interface CreateUserDto {
  email: string;
  firstname: string;
  lastname: string;
  manager: string;
  role: string;
  fullTime: boolean;
}
