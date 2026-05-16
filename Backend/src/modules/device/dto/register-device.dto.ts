import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString } from 'class-validator';

export class RegisterDeviceDto {
  @ApiPropertyOptional({ example: 'anon-device-001' })
  @IsOptional()
  @IsString()
  anonymousUserId?: string;

  @ApiProperty({ example: 'android-device-001' })
  @IsString()
  deviceId: string;

  @ApiPropertyOptional({ example: 'Samsung A54' })
  @IsOptional()
  @IsString()
  deviceName?: string;

  @ApiPropertyOptional({ example: 'Android 14' })
  @IsOptional()
  @IsString()
  osVersion?: string;
}
