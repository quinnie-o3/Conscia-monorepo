import {
  IsArray,
  IsNumber,
  IsOptional,
  IsString,
  Min,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class UsageSessionItemDto {
  @ApiPropertyOptional({ example: 'session-001' })
  @IsOptional()
  @IsString()
  externalId?: string;

  @ApiProperty({ example: 'com.google.android.youtube' })
  @IsString()
  packageName: string;

  @ApiPropertyOptional({ example: 'YouTube' })
  @IsOptional()
  @IsString()
  appName?: string;

  @ApiPropertyOptional({ example: 'learning' })
  @IsOptional()
  @IsString()
  purposeTag?: string;

  @ApiProperty({ example: '2026-05-15T08:00:00.000Z' })
  @IsString()
  startTime: string;

  @ApiProperty({ example: '2026-05-15T08:10:00.000Z' })
  @IsString()
  endTime: string;

  @ApiProperty({ example: 600, minimum: 0 })
  @IsNumber()
  @Min(0)
  durationSeconds: number;
}

export class SyncUsageSessionsDto {
  @ApiProperty({ example: 'anon-device-001' })
  @IsString()
  anonymousUserId: string;

  @ApiProperty({ example: 'android-device-001' })
  @IsString()
  deviceId: string;

  @ApiProperty({ type: [UsageSessionItemDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UsageSessionItemDto)
  sessions: UsageSessionItemDto[];
}
