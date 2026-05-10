import {
  BadRequestException,
  Controller,
  Get,
  Headers,
  Query,
} from '@nestjs/common';
import { StatsService } from './stats.service';
import { DailyStatsQueryDto } from './dto/daily-stats-query.dto';
import { UsageByPurposeQueryDto } from './dto/usage-by-purpose-query.dto';

@Controller(['stats', 'v1/stats'])
export class StatsController {
  constructor(private readonly statsService: StatsService) {}

  private resolveDeviceId(
    queryDeviceId: string | undefined,
    headerDeviceId: string | undefined,
  ) {
    const deviceId = queryDeviceId?.trim() || headerDeviceId?.trim();

    if (!deviceId) {
      throw new BadRequestException('deviceId is required');
    }

    return deviceId;
  }

  @Get('daily')
  async getDailySummary(
    @Query() query: DailyStatsQueryDto,
    @Headers('x-device-id') headerDeviceId?: string,
  ) {
    const deviceId = this.resolveDeviceId(query.deviceId, headerDeviceId);
    const data = await this.statsService.getDailySummary(
      query.anonymousUserId,
      deviceId,
      query.date,
    );

    return {
      success: true,
      message: 'Daily summary retrieved successfully',
      data,
    };
  }

  @Get('usage-by-purpose')
  async getUsageByPurpose(
    @Query() query: UsageByPurposeQueryDto,
    @Headers('x-device-id') headerDeviceId?: string,
    @Headers('x-timezone') headerTimeZone?: string,
  ) {
    const deviceId = this.resolveDeviceId(query.deviceId, headerDeviceId);
    const timeZone = query.timezone?.trim() || headerTimeZone?.trim();
    const data = await this.statsService.getUsageByPurpose(
      query.anonymousUserId,
      deviceId,
      query.from,
      query.to,
      query.period,
      query.date,
      timeZone,
    );

    return {
      success: true,
      message: 'Usage by purpose retrieved successfully',
      data,
    };
  }
}
