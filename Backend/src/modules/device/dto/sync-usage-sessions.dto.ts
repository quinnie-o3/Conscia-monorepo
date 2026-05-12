import {
  IsArray,
  IsNumber,
  IsOptional,
  IsString,
  Min,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';

export class UsageSessionItemDto {
  @IsOptional()
  @IsString()
  externalId?: string;

  @IsString()
  packageName: string;

  @IsOptional()
  @IsString()
  appName?: string;

  @IsOptional()
  @IsString()
  purposeTag?: string;

  @IsString()
  startTime: string;

  @IsString()
  endTime: string;

  @IsNumber()
  @Min(0)
  durationSeconds: number;
}

export class SyncUsageSessionsDto {
  @IsString()
  anonymousUserId: string;

  @IsString()
  deviceId: string;

  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UsageSessionItemDto)
  sessions: UsageSessionItemDto[];
}
