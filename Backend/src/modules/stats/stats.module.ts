import { Module } from '@nestjs/common';
import { UsageSessionModule } from '../usage-session/usage-session.module';
import { TrackingRuleModule } from '../tracking-rule/tracking-rule.module';
import { StatsController } from './stats.controller';
import { StatsService } from './stats.service';

@Module({
  imports: [UsageSessionModule, TrackingRuleModule],
  controllers: [StatsController],
  providers: [StatsService],
})
export class StatsModule {}