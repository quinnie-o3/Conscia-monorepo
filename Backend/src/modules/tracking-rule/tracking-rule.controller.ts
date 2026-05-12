import { Body, Controller, Delete, Get, Post, Query } from '@nestjs/common';
import { TrackingRuleService } from './tracking-rule.service';
import { UpsertTrackingRuleDto } from './dto/upsert-tracking-rule.dto';

@Controller('tracking-rules')
export class TrackingRuleController {
  constructor(
    private readonly trackingRuleService: TrackingRuleService,
  ) {}

  @Post()
  async upsert(@Body() dto: UpsertTrackingRuleDto) {
    const rule = await this.trackingRuleService.upsert(dto);

    return {
      success: true,
      message: 'Tracking rule saved successfully',
      data: rule,
    };
  }

  @Get()
  async findAll(
    @Query('anonymousUserId') anonymousUserId: string,
    @Query('deviceId') deviceId: string,
  ) {
    const rules = await this.trackingRuleService.findAll(
      anonymousUserId,
      deviceId,
    );

    return {
      success: true,
      message: 'Tracking rules retrieved successfully',
      data: rules,
    };
  }

  @Delete()
  async deleteOne(
    @Query('anonymousUserId') anonymousUserId: string,
    @Query('deviceId') deviceId: string,
    @Query('packageName') packageName: string,
  ) {
    await this.trackingRuleService.deleteOne(
      anonymousUserId,
      deviceId,
      packageName,
    );

    return {
      success: true,
      message: 'Tracking rule deleted successfully',
    };
  }
}