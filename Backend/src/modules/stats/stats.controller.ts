import {
  BadRequestException,
  Controller,
  Get,
  Headers,
  Query,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiHeader,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { StatsService } from './stats.service';
import { DailyStatsQueryDto } from './dto/daily-stats-query.dto';
import { UsageByPurposeQueryDto } from './dto/usage-by-purpose-query.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface';

@ApiTags('Stats')
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

  @ApiOperation({ summary: 'Get daily usage summary' })
  @ApiHeader({
    name: 'x-device-id',
    required: false,
    example: 'android-device-001',
  })
  @ApiBearerAuth('JWT-auth')
  @ApiResponse({
    status: 200,
    description: 'Daily usage summary retrieved successfully.',
  })
  @ApiResponse({
    status: 400,
    description: 'deviceId or date is missing or invalid.',
  })
  @Get('daily')
  @UseGuards(JwtAuthGuard)
  async getDailySummary(
    @Query() query: DailyStatsQueryDto,
    @CurrentUser() user: AuthenticatedUser,
    @Headers('x-device-id') headerDeviceId?: string,
  ) {
    const deviceId = this.resolveDeviceId(query.deviceId, headerDeviceId);
    const data = await this.statsService.getDailySummary(
      query.anonymousUserId,
      deviceId,
      query.date,
      user.userId,
    );

    return {
      success: true,
      message: 'Daily summary retrieved successfully',
      data,
    };
  }

  @ApiOperation({ summary: 'Get usage grouped by purpose' })
  @ApiHeader({
    name: 'x-device-id',
    required: false,
    example: 'android-device-001',
  })
  @ApiHeader({
    name: 'x-timezone',
    required: false,
    example: 'Asia/Saigon',
  })
  @ApiBearerAuth('JWT-auth')
  @ApiResponse({
    status: 200,
    description: 'Usage grouped by purpose retrieved successfully.',
  })
  @ApiResponse({
    status: 400,
    description: 'deviceId or date range query is missing or invalid.',
  })
  @Get('usage-by-purpose')
  @UseGuards(JwtAuthGuard)
  async getUsageByPurpose(
    @Query() query: UsageByPurposeQueryDto,
    @CurrentUser() user: AuthenticatedUser,
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
      user.userId,
    );

    return {
      success: true,
      message: 'Usage by purpose retrieved successfully',
      data,
    };
  }
}
