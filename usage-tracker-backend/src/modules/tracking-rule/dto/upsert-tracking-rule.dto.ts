import {
  IsBoolean,
  IsNumber,
  IsOptional,
  IsString,
  Min,
} from 'class-validator';

export class UpsertTrackingRuleDto {
  @IsOptional()
  @IsString()
  anonymousUserId?: string;

  @IsString()
  deviceId: string;

  @IsString()
  packageName: string;

  @IsString()
  appName: string;

  @IsOptional()
  @IsString()
  purposeTag?: string;

  @IsOptional()
  @IsString()
  intentionLabel?: string;

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
