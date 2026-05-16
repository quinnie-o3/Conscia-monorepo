import { Body, Controller, Get, NotFoundException, Patch, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import type { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface';
import { UserService } from './user.service';
import { UpdateUserDto } from './dto/update-user.dto';

@ApiTags('Users')
@ApiBearerAuth('JWT-auth')
@Controller('users')
@UseGuards(JwtAuthGuard)
export class UserController {
  constructor(private readonly userService: UserService) {}

  @ApiOperation({ summary: 'Get current user profile' })
  @Get('profile')
  async getProfile(@CurrentUser() user: AuthenticatedUser) {
    const userData = await this.userService.findById(user.userId);
    if (!userData) {
      throw new NotFoundException('User not found');
    }
    return {
      success: true,
      data: this.userService.toPublicUser(userData),
    };
  }

  @ApiOperation({ summary: 'Update user profile' })
  @Patch('profile')
  async updateProfile(
    @CurrentUser() user: AuthenticatedUser,
    @Body() dto: UpdateUserDto,
  ) {
    const updatedUser = await this.userService.update(user.userId, dto);
    if (!updatedUser) {
      throw new NotFoundException('User not found');
    }
    return {
      success: true,
      message: 'Profile updated successfully',
      data: this.userService.toPublicUser(updatedUser),
    };
  }
}
