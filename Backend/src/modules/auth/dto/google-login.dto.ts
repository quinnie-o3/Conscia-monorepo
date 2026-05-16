import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, MinLength } from 'class-validator';

export class GoogleLoginDto {
  @ApiProperty({ example: 'eyJhbGciOiJSUzI1NiIsImtpZCI6...' })
  @IsString()
  @MinLength(10)
  idToken: string;

  @ApiPropertyOptional({ example: 'android-device-001' })
  @IsOptional()
  @IsString()
  tempDeviceId?: string;
}
