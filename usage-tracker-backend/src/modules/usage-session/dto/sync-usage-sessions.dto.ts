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
  @IsOptional()
  @IsString()
  externalId?: string;

  @IsOptional()
  @IsString()
  deviceId?: string;

  @IsString()
  packageName: string;

  @IsOptional()
  @IsString()
  appName?: string;

  @IsOptional()
  @IsString()
  purposeTag?: string;

  @IsOptional()
  @IsString()
  intentionLabel?: string;

  @ValidateIf((_, value) => value !== undefined)
  @IsDateString()
  startTime?: string;

  @ValidateIf((_, value) => value !== undefined)
  @IsDateString()
  endTime?: string;

  @ValidateIf((object: UsageSessionItemDto) => !object.startTime)
  @IsDateString()
  startedAt?: string;

  @ValidateIf((object: UsageSessionItemDto) => !object.endTime)
  @IsDateString()
  endedAt?: string;

  @Type(() => Number)
  @IsNumber()
  @Min(0)
  durationSeconds: number;

  @IsOptional()
  @IsString()
  @Matches(/^\d{4}-\d{2}-\d{2}$/)
  deviceLocalDate?: string;

  @IsOptional()
  @IsString()
  deviceTimezone?: string;

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  timezoneOffsetMinutes?: number;

  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  trackingEnabled?: boolean;

  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  warningEnabled?: boolean;

  @IsOptional()
  @Type(() => Number)
  @IsInt()
  @Min(0)
  dailyLimitMinutes?: number;

  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  tags?: string[];

  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  isClassified?: boolean;
}

export class SyncUsageSessionsDto {
  @IsOptional()
  @IsString()
  anonymousUserId?: string;

  @IsOptional()
  @IsString()
  deviceId?: string;

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UsageSessionItemDto)
  sessions: UsageSessionItemDto[];
}
