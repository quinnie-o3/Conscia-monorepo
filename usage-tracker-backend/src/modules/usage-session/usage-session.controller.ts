import { Body, Controller, Post } from '@nestjs/common';
import { SyncUsageSessionsDto } from './dto/sync-usage-sessions.dto';
import { UsageSessionService } from './usage-session.service';

@Controller([
  'usage-sessions',
  'sessions',
  'v1/usage-sessions',
  'v1/sessions',
])
export class UsageSessionController {
  constructor(
    private readonly usageSessionService: UsageSessionService,
  ) {}

  @Post('sync')
  async sync(@Body() dto: SyncUsageSessionsDto) {
    const result = await this.usageSessionService.sync(dto);

    return {
      success: true,
      message: 'Usage sessions synced successfully',
      data: result,
    };
  }
}
