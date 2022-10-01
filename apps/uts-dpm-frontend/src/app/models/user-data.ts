export default class UserData {
  token: string = '';
  exp: number = 0;
  role: string = '';
  username: string = '';

  clear() {
    this.token = '';
    this.exp = 0;
    this.role = '';
    this.username = '';
  }
}
