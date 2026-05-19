import {
  ConflictException,
  Injectable,
  UnauthorizedException,
  BadRequestException,
  NotFoundException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { JwtService } from '@nestjs/jwt';
import { Model } from 'mongoose';

import { normalizeOptionalString } from '../../common/device-identity.util';
import { DeviceService } from '../device/device.service';
import { Device } from '../device/device.schema';
import { TrackingRule } from '../tracking-rule/tracking-rule.schema';
import { UsageSession } from '../usage-session/usage-session.schema';
import { UserDocument } from '../user/user.schema';
import {
  GoogleProfileData,
  PublicUser,
  UserService,
} from '../user/user.service';
import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';
import { ResetPasswordDto } from './dto/reset-password.dto';
import type {
  GoogleAuthenticatedProfile,
  JwtPayload,
} from './interfaces/authenticated-user.interface';

type AuthResponse = {
  accessToken: string;
  user: PublicUser;
};

@Injectable()
export class AuthService {
  constructor(
    private readonly userService: UserService,
    private readonly jwtService: JwtService,
    private readonly deviceService: DeviceService,
    @InjectModel(TrackingRule.name)
    private readonly trackingRuleModel: Model<TrackingRule>,
    @InjectModel(UsageSession.name)
    private readonly usageSessionModel: Model<UsageSession>,
    @InjectModel(Device.name)
    private readonly deviceModel: Model<Device>,
  ) {}

  private async buildAuthResponse(user: UserDocument): Promise<AuthResponse> {
    const publicUser = this.userService.toPublicUser(user);

    if (!publicUser) {
      throw new UnauthorizedException('Invalid user');
    }

    const payload: JwtPayload = {
      displayName: publicUser.displayName,
      email: publicUser.email,
      sub: publicUser.id,
    };

    return {
      accessToken: await this.jwtService.signAsync(payload),
      user: publicUser,
    };
  }

  async register(dto: RegisterDto) {
    const user = await this.userService.createLocalUser({
      avatarUrl: dto.avatarUrl,
      displayName: dto.displayName,
      email: dto.email,
      password: dto.password,
    });

    await this.migrateAnonymousData(user.id, dto.tempDeviceId);

    return this.buildAuthResponse(user);
  }

  async validateLocalUser(email: string, password: string) {
    const user = await this.userService.findByEmail(email, true);

    if (!user) {
      throw new UnauthorizedException('Invalid email or password');
    }

    const isPasswordValid = await this.userService.comparePassword(
      user,
      password,
    );

    if (!isPasswordValid) {
      throw new UnauthorizedException('Invalid email or password');
    }

    return user;
  }

  async login(user: UserDocument, dto?: Pick<LoginDto, 'tempDeviceId'>) {
    await this.migrateAnonymousData(user.id, dto?.tempDeviceId);

    const latestUser = await this.userService.findById(user.id);

    if (!latestUser) {
      throw new UnauthorizedException('User account no longer exists');
    }

    return this.buildAuthResponse(latestUser);
  }

  async authenticateWithGoogle(
    profile: GoogleAuthenticatedProfile,
    tempDeviceId?: string,
  ) {
    let user = await this.userService.findByGoogleId(profile.googleId);

    if (!user && profile.email) {
      user = await this.userService.findByEmail(profile.email);

      if (user && user.googleId && user.googleId !== profile.googleId) {
        throw new ConflictException('Google account is already linked');
      }
    }

    if (user) {
      const linkedUser = await this.userService.attachGoogleAccount(
        user.id,
        profile as GoogleProfileData,
      );

      if (!linkedUser) {
        throw new UnauthorizedException('Unable to link Google account');
      }

      user = linkedUser;
    } else {
      user = await this.userService.createGoogleUser(
        profile as GoogleProfileData,
      );
    }

    if (!user) {
      throw new UnauthorizedException('Unable to create or link Google account');
    }

    await this.migrateAnonymousData(user.id, tempDeviceId);

    return this.buildAuthResponse(user);
  }

  private async verifyGoogleIdToken(idToken: string): Promise<GoogleAuthenticatedProfile> {
    const response = await (globalThis as any).fetch(
      `https://oauth2.googleapis.com/tokeninfo?id_token=${encodeURIComponent(idToken)}`,
    );

    if (!response?.ok) {
      throw new UnauthorizedException('Invalid Google ID token');
    }

    const payload = await response.json();

    if (payload.email_verified !== 'true' && payload.email_verified !== true) {
      throw new UnauthorizedException('Google email is not verified');
    }

    if (!payload.sub || !payload.email) {
      throw new UnauthorizedException('Google token is missing required profile data');
    }

    return {
      avatarUrl: payload.picture,
      displayName: payload.name,
      email: payload.email,
      googleId: payload.sub,
    };
  }

  async authenticateWithGoogleIdToken(idToken: string, tempDeviceId?: string) {
    const profile = await this.verifyGoogleIdToken(idToken);
    return this.authenticateWithGoogle(profile, tempDeviceId);
  }

  async resetPassword(dto: ResetPasswordDto) {
    if (!dto.newPassword || dto.newPassword.length < 8) {
      throw new BadRequestException('Password must be at least 8 characters');
    }

    const user = await this.userService.updatePasswordByEmail(
      dto.email,
      dto.newPassword,
    );

    if (!user) {
      throw new NotFoundException('No account found for this email');
    }

    return undefined;
  }

  async migrateAnonymousData(userId: string, tempDeviceId?: string) {
    const normalizedTempDeviceId = normalizeOptionalString(tempDeviceId);

    if (!normalizedTempDeviceId) {
      return {
        migratedDevices: 0,
        migratedRules: 0,
        migratedSessions: 0,
      };
    }

    const anonymousUserId =
      await this.deviceService.resolveAnonymousUserIdForDevice(
        normalizedTempDeviceId,
      );

    const [ruleResult, sessionResult] = await Promise.all([
      this.trackingRuleModel
        .updateMany(
          {
            deviceId: normalizedTempDeviceId,
            userId: null,
          },
          {
            $set: {
              anonymousUserId,
              userId,
            },
          },
        )
        .exec(),
      this.usageSessionModel
        .updateMany(
          {
            clientDeviceId: normalizedTempDeviceId,
            userId: null,
          },
          {
            $set: {
              anonymousUserId,
              userId,
            },
          },
        )
        .exec(),
    ]);

    const device = await this.deviceService.attachUser(
      normalizedTempDeviceId,
      userId,
      anonymousUserId,
    );

    await this.deviceModel
      .updateMany(
        {
          anonymousUserId,
          deviceId: normalizedTempDeviceId,
          userId: null,
        },
        {
          $set: { userId },
        },
      )
      .exec();

    return {
      migratedDevices: device ? 1 : 0,
      migratedRules: ruleResult.modifiedCount,
      migratedSessions: sessionResult.modifiedCount,
    };
  }
}