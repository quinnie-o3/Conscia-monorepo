export interface JwtPayload {
  displayName?: string;
  email?: string;
  sub: string;
}

export interface AuthenticatedUser {
  displayName?: string;
  email?: string;
  userId: string;
}

export interface GoogleAuthenticatedProfile {
  avatarUrl?: string;
  displayName?: string;
  email?: string;
  googleId: string;
}
