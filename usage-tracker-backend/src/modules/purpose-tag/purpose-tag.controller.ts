import { Controller, Get } from '@nestjs/common';
import { PurposeTagService } from './purpose-tag.service';

@Controller('purpose-tags')
export class PurposeTagController {
  constructor(
    private readonly purposeTagService: PurposeTagService,
  ) {}

  @Get()
  async findAll() {
    return {
      success: true,
      message: 'Purpose tags retrieved successfully',
      data: this.purposeTagService.findAll(),
    };
  }
}