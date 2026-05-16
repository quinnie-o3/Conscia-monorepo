import { Module } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtModule } from '@nestjs/jwt';
import { MongooseModule } from '@nestjs/mongoose';
import { PassportModule } from '@nestjs/passport';

import { DeviceModule } from '../device/device.module';
import { Device, DeviceSchema } from '../device/device.schema';
import {
  TrackingRule,
  TrackingRuleSchema,
} from '../tracking-rule/tracking-rule.schema';
import {
  UsageSession,
  UsageSessionSchema,
} from '../usage-session/usage-session.schema';
import { UserModule } from '../user/user.module';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { GoogleAuthGuard } from './guards/google-auth.guard';
import { JwtAuthGuard } from './guards/jwt-auth.guard';
import { LocalAuthGuard } from './guards/local-auth.guard';
import { GoogleStrategy } from './strategies/google.strategy';
import { JwtStrategy } from './strategies/jwt.strategy';
import { LocalStrategy } from './strategies/local.strategy';

@Module({
  imports: [
    DeviceModule,
    PassportModule.register({ session: false }),
    JwtModule.registerAsync({
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => ({
        secret:
          configService.get<string>('JWT_SECRET') || 'conscia-dev-secret',
        signOptions: {
          expiresIn:
            (configService.get<string>('JWT_EXPIRES_IN') || '7d') as never,
        },
      }),
    }),
    UserModule,
    MongooseModule.forFeature([
      { name: Device.name, schema: DeviceSchema },
      { name: TrackingRule.name, schema: TrackingRuleSchema },
      { name: UsageSession.name, schema: UsageSessionSchema },
    ]),
  ],
  controllers: [AuthController],
  providers: [
    AuthService,
    GoogleAuthGuard,
    JwtAuthGuard,
    LocalAuthGuard,
    LocalStrategy,
    JwtStrategy,
    GoogleStrategy,
  ],
  exports: [AuthService, JwtAuthGuard],
})
export class AuthModule {}
