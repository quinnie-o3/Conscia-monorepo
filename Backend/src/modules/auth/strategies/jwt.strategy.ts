import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PassportStrategy } from '@nestjs/passport';
import { ExtractJwt, Strategy } from 'passport-jwt';
import { Types } from 'mongoose';

import type {
  AuthenticatedUser,
  JwtPayload,
} from '../interfaces/authenticated-user.interface';

@Injectable()
export class JwtStrategy extends PassportStrategy(Strategy) {
  constructor(configService: ConfigService) {
    super({
      ignoreExpiration: false,
      jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
      secretOrKey:
        configService.get<string>('JWT_SECRET') || 'conscia-dev-secret',
    });
  }

  validate(payload: JwtPayload): AuthenticatedUser {
    if (!payload.sub || !Types.ObjectId.isValid(payload.sub)) {
      throw new UnauthorizedException('Invalid token subject');
    }

    return {
      displayName: payload.displayName,
      email: payload.email,
      userId: payload.sub,
    };
  }
}
