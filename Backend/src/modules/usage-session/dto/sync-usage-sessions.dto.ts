import {
  ApiProperty,
  ApiPropertyOptional,
} from '@nestjs/swagger';
import { Type } from 'class-transformer';
import {
  IsArray,
  IsBoolean,
  IsDateString,
  IsInt,
  IsNumber,
  IsOptional,
  IsString,
  Matches,
  Min,
  ValidateIf,
  ValidateNested,
} from 'class-validator';

export class UsageSessionItemDto {
  @ApiPropertyOptional({ example: 'session-001' })
  @IsOptional()
  @IsString()
  externalId?: string;

  @ApiPropertyOptional({ example: 'android-device-001' })
  @IsOptional()
  @IsString()
  deviceId?: string;

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

  @ApiPropertyOptional({ example: 'Study recap' })
  @IsOptional()
  @IsString()
  intentionLabel?: string;

  @ApiPropertyOptional({ example: '2026-05-15T08:00:00.000Z' })
  @ValidateIf((_, value) => value !== undefined)
  @IsDateString()
  startTime?: string;

  @ApiPropertyOptional({ example: '2026-05-15T08:10:00.000Z' })
  @ValidateIf((_, value) => value !== undefined)
  @IsDateString()
  endTime?: string;

  @ApiPropertyOptional({ example: '2026-05-15T08:00:00.000Z' })
  @ValidateIf((object: UsageSessionItemDto) => !object.startTime)
  @IsDateString()
  startedAt?: string;

  @ApiPropertyOptional({ example: '2026-05-15T08:10:00.000Z' })
  @ValidateIf((object: UsageSessionItemDto) => !object.endTime)
  @IsDateString()
  endedAt?: string;

  @ApiProperty({ example: 600, minimum: 0 })
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  durationSeconds: number;

  @ApiPropertyOptional({ example: '2026-05-15' })
  @IsOptional()
  @IsString()
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  deviceLocalDate?: string;

  @ApiPropertyOptional({ example: 'Asia/Saigon' })
  @IsOptional()
  @IsString()
  deviceTimezone?: string;

  @ApiPropertyOptional({ example: 420 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  timezoneOffsetMinutes?: number;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  trackingEnabled?: boolean;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  warningEnabled?: boolean;

  @ApiPropertyOptional({ example: 60, minimum: 0 })
  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  dailyLimitMinutes?: number;

  @ApiPropertyOptional({ example: ['study', 'focus'], type: [String] })
  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  tags?: string[];

  @ApiPropertyOptional({ example: false })
  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  isClassified?: boolean;
}

export class SyncUsageSessionsDto {
  @ApiPropertyOptional({ example: 'android-device-001' })
  @IsOptional()
  @IsString()
  deviceId?: string;

  @ApiProperty({ type: [UsageSessionItemDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UsageSessionItemDto)
  sessions: UsageSessionItemDto[];
}
