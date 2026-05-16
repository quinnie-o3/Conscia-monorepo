import {
  Body,
  Controller,
  Get,
  Post,
  Query,
  Req,
  UseGuards,
} from '@nestjs/common';
import { Request } from 'express';
import {
  ApiBearerAuth,
  ApiBody,
  ApiOperation,
  ApiQuery,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';

import { LoginDto } from './dto/login.dto';
import { RegisterDto } from './dto/register.dto';
import { GoogleLoginDto } from './dto/google-login.dto';
import { ResetPasswordDto } from './dto/reset-password.dto';
import { GoogleAuthGuard } from './guards/google-auth.guard';
import { LocalAuthGuard } from './guards/local-auth.guard';
import type { GoogleAuthenticatedProfile } from './interfaces/authenticated-user.interface';
import { AuthService } from './auth.service';
import type { UserDocument } from '../user/user.schema';

type LocalAuthRequest = Request & {
  user: UserDocument;
};

type GoogleAuthRequest = Request & {
  user: GoogleAuthenticatedProfile;
};

@ApiTags('Authentication')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @ApiOperation({
    summary: 'Start Google OAuth login flow',
  })
  @ApiQuery({
    name: 'tempDeviceId',
    required: false,
    example: 'android-device-001',
  })
  @ApiResponse({
    status: 302,
    description: 'Redirects the client to Google OAuth.',
  })
  @Get('google')
  @UseGuards(GoogleAuthGuard)
  googleAuth() {
    return undefined;
  }

  @ApiOperation({
    summary: 'Google OAuth callback',
  })
  @ApiQuery({
    name: 'state',
    required: false,
    example: 'android-device-001',
  })
  @ApiResponse({
    status: 200,
    description: 'Google login completed successfully.',
  })
  @ApiResponse({
    status: 401,
    description: 'Google authentication failed.',
  })
  @Get('google/callback')
  @UseGuards(GoogleAuthGuard)
  async googleAuthCallback(
    @Req() request: GoogleAuthRequest,
    @Query('state') tempDeviceId?: string,
  ) {
    const data = await this.authService.authenticateWithGoogle(
      request.user,
      tempDeviceId,
    );

    return {
      success: true,
      message: 'Google login successful',
      data,
    };
  }

  @ApiOperation({
    summary: 'Login with email and password',
  })
  @ApiBody({ type: LoginDto })
  @ApiResponse({
    status: 201,
    description: 'Login completed successfully.',
  })
  @ApiResponse({
    status: 400,
    description: 'Request body validation failed.',
  })
  @ApiResponse({
    status: 401,
    description: 'Invalid email or password.',
  })
  @Post('login')
  @UseGuards(LocalAuthGuard)
  async login(
    @Req() request: LocalAuthRequest,
    @Body() dto: LoginDto,
  ) {
    const data = await this.authService.login(request.user, dto);

    return {
      success: true,
      message: 'Login successful',
      data,
    };
  }

  @ApiOperation({
    summary: 'Login with Google ID token from mobile clients',
  })
  @ApiBody({ type: GoogleLoginDto })
  @ApiResponse({
    status: 201,
    description: 'Google login completed successfully.',
  })
  @Post('google')
  async googleLogin(@Body() dto: GoogleLoginDto) {
    const data = await this.authService.authenticateWithGoogleIdToken(
      dto.idToken,
      dto.tempDeviceId,
    );

    return {
      success: true,
      message: 'Google login successful',
      data,
    };
  }

  @ApiOperation({
    summary: 'Register a new account',
  })
  @ApiBody({ type: RegisterDto })
  @ApiResponse({
    status: 201,
    description: 'Account registered successfully.',
  })
  @ApiResponse({
    status: 400,
    description: 'Request body validation failed.',
  })
  @ApiResponse({
    status: 409,
    description: 'Email is already registered.',
  })
  @Post('register')
  async register(@Body() dto: RegisterDto) {
    const data = await this.authService.register(dto);

    return {
      success: true,
      message: 'Registration successful',
      data,
    };
  }

  @ApiOperation({
    summary: 'Reset password by email',
  })
  @ApiBody({ type: ResetPasswordDto })
  @ApiResponse({
    status: 201,
    description: 'Password updated successfully.',
  })
  @Post('reset-password')
  async resetPassword(@Body() dto: ResetPasswordDto) {
    await this.authService.resetPassword(dto);

    return {
      success: true,
      message: 'Password updated successfully',
      data: null,
    };
  }
}
