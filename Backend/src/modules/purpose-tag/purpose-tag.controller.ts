import { Controller, Get } from '@nestjs/common';
import { ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { PurposeTagService } from './purpose-tag.service';

@ApiTags('Purpose Tags')
@Controller('purpose-tags')
export class PurposeTagController {
  constructor(
    private readonly purposeTagService: PurposeTagService,
  ) {}

  @ApiOperation({ summary: 'List available purpose tags' })
  @ApiResponse({
    status: 200,
    description: 'Purpose tags retrieved successfully.',
  })
  @Get()
  async findAll() {
    return {
      success: true,
      message: 'Purpose tags retrieved successfully',
      data: this.purposeTagService.findAll(),
    };
  }
}
