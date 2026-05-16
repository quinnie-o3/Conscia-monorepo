import {
  Body,
  Controller,
  Delete,
  Get,
  Post,
  Query,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiBody,
  ApiOperation,
  ApiQuery,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import type { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface';
import { TrackingRuleService } from './tracking-rule.service';
import { UpsertTrackingRuleDto } from './dto/upsert-tracking-rule.dto';

@ApiTags('Tracking Rules')
@ApiBearerAuth('JWT-auth')
@UseGuards(JwtAuthGuard)
@Controller('tracking-rules')
export class TrackingRuleController {
  constructor(
    private readonly trackingRuleService: TrackingRuleService,
  ) {}

  @ApiOperation({ summary: 'Create or update a tracking rule' })
  @ApiBody({ type: UpsertTrackingRuleDto })
  @ApiResponse({
    status: 201,
    description: 'Tracking rule created or updated successfully.',
  })
  @ApiResponse({
    status: 400,
    description: 'Request body validation failed.',
  })
  @ApiResponse({
    status: 401,
    description: 'JWT bearer token is missing or invalid.',
  })
  @Post()
  async upsert(
    @CurrentUser() user: AuthenticatedUser,
    @Body() dto: UpsertTrackingRuleDto,
  ) {
    const rule = await this.trackingRuleService.upsert(user.userId, dto);

    return {
      success: true,
      message: 'Tracking rule saved successfully',
      data: rule,
    };
  }

  @ApiOperation({ summary: 'List tracking rules for a device' })
  @ApiQuery({
    name: 'deviceId',
    required: true,
    example: 'android-device-001',
  })
  @ApiResponse({
    status: 200,
    description: 'Tracking rules retrieved successfully.',
  })
  @ApiResponse({
    status: 401,
    description: 'JWT bearer token is missing or invalid.',
  })
  @Get()
  async findAll(
    @CurrentUser() user: AuthenticatedUser,
    @Query('deviceId') deviceId: string,
  ) {
    const rules = await this.trackingRuleService.findAllForUser(
      user.userId,
      deviceId,
    );

    return {
      success: true,
      message: 'Tracking rules retrieved successfully',
      data: rules,
    };
  }

  @ApiOperation({ summary: 'Delete a tracking rule by package name' })
  @ApiQuery({
    name: 'deviceId',
    required: true,
    example: 'android-device-001',
  })
  @ApiQuery({
    name: 'packageName',
    required: true,
    example: 'com.google.android.youtube',
  })
  @ApiResponse({
    status: 200,
    description: 'Tracking rule deleted successfully.',
  })
  @ApiResponse({
    status: 401,
    description: 'JWT bearer token is missing or invalid.',
  })
  @Delete()
  async deleteOne(
    @CurrentUser() user: AuthenticatedUser,
    @Query('deviceId') deviceId: string,
    @Query('packageName') packageName: string,
  ) {
    await this.trackingRuleService.deleteOneForUser(
      user.userId,
      deviceId,
      packageName,
    );

    return {
      success: true,
      message: 'Tracking rule deleted successfully',
    };
  }
}
