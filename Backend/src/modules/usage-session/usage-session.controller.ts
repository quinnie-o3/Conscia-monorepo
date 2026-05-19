import {
  Body,
  Controller,
  Post,
  UnauthorizedException,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiBody,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import type { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface';
import { SyncUsageSessionsDto } from './dto/sync-usage-sessions.dto';
import { UsageSessionService } from './usage-session.service';

@ApiTags('Usage Sessions')
@ApiBearerAuth('JWT-auth')
@UseGuards(JwtAuthGuard)
@Controller(['usage-sessions', 'sessions', 'v1/usage-sessions', 'v1/sessions'])
export class UsageSessionController {
  constructor(private readonly usageSessionService: UsageSessionService) {}

  private requireUser(user: AuthenticatedUser | undefined) {
    if (!user?.userId) {
      throw new UnauthorizedException('Authenticated user is required');
    }

    return user;
  }

  @ApiOperation({ summary: 'Sync usage sessions from device to backend' })
  @ApiBody({ type: SyncUsageSessionsDto })
  @ApiResponse({
    status: 201,
    description: 'Usage sessions synced successfully.',
  })
  @ApiResponse({
    status: 400,
    description: 'Request body validation failed.',
  })
  @ApiResponse({
    status: 401,
    description: 'JWT bearer token is missing or invalid.',
  })
  @Post('sync')
  async sync(
    @CurrentUser() user: AuthenticatedUser,
    @Body() dto: SyncUsageSessionsDto,
  ) {
    const currentUser = this.requireUser(user);
    const result = await this.usageSessionService.sync(currentUser.userId, dto);

    return {
      success: true,
      message: 'Usage sessions synced successfully',
      data: result,
    };
  }
}
