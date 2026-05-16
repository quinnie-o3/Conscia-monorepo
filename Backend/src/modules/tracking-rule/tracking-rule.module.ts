import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { DeviceModule } from '../device/device.module';
import {
  TrackingRule,
  TrackingRuleSchema,
} from './tracking-rule.schema';
import { TrackingRuleController } from './tracking-rule.controller';
import { TrackingRuleService } from './tracking-rule.service';

@Module({
  imports: [
    DeviceModule,
    MongooseModule.forFeature([
      { name: TrackingRule.name, schema: TrackingRuleSchema },
    ]),
  ],
  controllers: [TrackingRuleController],
  providers: [TrackingRuleService],
  exports: [TrackingRuleService, MongooseModule],
})
export class TrackingRuleModule {}
