import { Module } from '@nestjs/common';
import { PurposeTagController } from './purpose-tag.controller';
import { PurposeTagService } from './purpose-tag.service';

@Module({
  controllers: [PurposeTagController],
  providers: [PurposeTagService]
})
export class PurposeTagModule {}
