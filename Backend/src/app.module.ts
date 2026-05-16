import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { MongooseModule } from '@nestjs/mongoose';

import { AuthModule } from './modules/auth/auth.module';
import { DeviceModule } from './modules/device/device.module';
import { TrackingRuleModule } from './modules/tracking-rule/tracking-rule.module';
import { UsageSessionModule } from './modules/usage-session/usage-session.module';
import { StatsModule } from './modules/stats/stats.module';
import { PurposeTagModule } from './modules/purpose-tag/purpose-tag.module';
import { IntentionModule } from './modules/intention/intention.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),

    MongooseModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => ({
        uri: configService.get<string>('MONGODB_URI'),
      }),
    }),

    AuthModule,
    DeviceModule,
    TrackingRuleModule,
    UsageSessionModule,
    StatsModule,
    PurposeTagModule,
    IntentionModule,
  ],
})
export class AppModule {}
