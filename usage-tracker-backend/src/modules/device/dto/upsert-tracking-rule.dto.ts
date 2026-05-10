import {
  IsBoolean,
  IsNumber,
  IsOptional,
  IsString,
  Min,
} from 'class-validator';

export class UpsertTrackingRuleDto {
  @IsString()
  anonymousUserId: string;

  @IsString()
  deviceId: string;

  @IsString()
  packageName: string;

  @IsString()
  appName: string;

  @IsString()
  purposeTag: string;

  @IsNumber()
  @Min(1)
  dailyLimitMinutes: number;

  @IsOptional()
  @IsBoolean()
  trackingEnabled?: boolean;

  @IsOptional()
  @IsBoolean()
  warningEnabled?: boolean;
}