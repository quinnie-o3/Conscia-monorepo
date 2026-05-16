import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PassportStrategy } from '@nestjs/passport';
import {
  Profile,
  Strategy,
  VerifyCallback,
} from 'passport-google-oauth20';

import type { GoogleAuthenticatedProfile } from '../interfaces/authenticated-user.interface';

@Injectable()
export class GoogleStrategy extends PassportStrategy(Strategy, 'google') {
  constructor(configService: ConfigService) {
    const clientID =
      configService.get<string>('GOOGLE_CLIENT_ID') ||
      'missing-google-client-id';
    const clientSecret =
      configService.get<string>('GOOGLE_CLIENT_SECRET') ||
      'missing-google-client-secret';

    super({
      callbackURL:
        configService.get<string>('GOOGLE_CALLBACK_URL') ||
        'http://localhost:3000/api/auth/google/callback',
      clientID,
      clientSecret,
      scope: ['email', 'profile'],
    });
  }

  validate(
    _accessToken: string,
    _refreshToken: string,
    profile: Profile,
    done: VerifyCallback,
  ) {
    const googleProfile: GoogleAuthenticatedProfile = {
      avatarUrl: profile.photos?.[0]?.value,
      displayName: profile.displayName,
      email: profile.emails?.[0]?.value?.toLowerCase(),
      googleId: profile.id,
    };

    done(null, googleProfile);
  }
}
