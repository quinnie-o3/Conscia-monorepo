import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsBoolean,
  IsNumber,
  IsOptional,
  IsString,
  Min,
} from 'class-validator';

export class UpsertTrackingRuleDto {
  @ApiProperty({ example: 'android-device-001' })
  @IsString()
  deviceId: string;

  @ApiProperty({ example: 'com.google.android.youtube' })
  @IsString()
  packageName: string;

  @ApiProperty({ example: 'YouTube' })
  @IsString()
  appName: string;

  @ApiPropertyOptional({ example: 'learning' })
  @IsOptional()
  @IsString()
  purposeTag?: string;

  @ApiPropertyOptional({ example: 'Study recap' })
  @IsOptional()
  @IsString()
  intentionLabel?: string;

  @ApiProperty({ example: 60, minimum: 1 })
  @IsNumber()
  @Min(1)
  dailyLimitMinutes: number;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @IsBoolean()
  trackingEnabled?: boolean;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @IsBoolean()
  warningEnabled?: boolean;

  @ApiPropertyOptional({ example: 5 })
  @IsOptional()
  @IsNumber()
  extensionMinutes?: number;

  @ApiPropertyOptional({ example: 1 })
  @IsOptional()
  @IsNumber()
  extensionCount?: number;

  @ApiPropertyOptional({ example: '2026-05-15' })
  @IsOptional()
  @IsString()
  lastExtensionDate?: string;
}
