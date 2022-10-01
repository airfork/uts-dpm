export default interface TokenPayload {
  sub: string;
  iat: number;
  exp: number;
  role: string;
}
