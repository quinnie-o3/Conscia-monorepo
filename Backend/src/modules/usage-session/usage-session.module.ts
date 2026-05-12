import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { DeviceModule } from '../device/device.module';

import {
  UsageSession,
  UsageSessionSchema,
} from './usage-session.schema';
import { UsageSessionController } from './usage-session.controller';
import { UsageSessionService } from './usage-session.service';

@Module({
  imports: [
    DeviceModule,
    MongooseModule.forFeature([
      { name: UsageSession.name, schema: UsageSessionSchema },
    ]),
  ],
  controllers: [UsageSessionController],
  providers: [UsageSessionService],
  exports: [UsageSessionService, MongooseModule],
})
export class UsageSessionModule {}
