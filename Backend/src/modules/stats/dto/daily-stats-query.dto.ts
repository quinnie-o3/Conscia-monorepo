import { ApiPropertyOptional, ApiProperty } from '@nestjs/swagger';
import { IsOptional, IsString, Matches } from 'class-validator';

export class DailyStatsQueryDto {
  @ApiPropertyOptional({ example: 'anon-device-001' })
  @IsOptional()
  @IsString()
  anonymousUserId?: string;

  @ApiPropertyOptional({ example: 'android-device-001' })
  @IsOptional()
  @IsString()
  deviceId?: string;

  @ApiProperty({ example: '2026-05-15' })
  @IsString()
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  date: string;
}
