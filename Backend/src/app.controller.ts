import { Controller, Get } from '@nestjs/common';
import { ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { AppService } from './app.service';

@ApiTags('Health')
@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @ApiOperation({ summary: 'Backend health check' })
  @ApiResponse({
    status: 200,
    description: 'Backend is running.',
  })
  @Get()
  getHello(): string {
    return this.appService.getHello();
  }
}
