import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Post,
  UnauthorizedException,
  UseGuards,
} from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import type { AuthenticatedUser } from '../auth/interfaces/authenticated-user.interface';
import { IntentionService } from './intention.service';

@ApiTags('Intentions')
@ApiBearerAuth('JWT-auth')
@Controller('intentions')
export class IntentionController {
  constructor(private readonly intentionService: IntentionService) {}

  private requireUser(user: AuthenticatedUser | undefined) {
    if (!user?.userId) {
      throw new UnauthorizedException('Authenticated user is required');
    }

    return user;
  }

  @ApiOperation({ summary: 'List all available intentions (system + user)' })
  @Get()
  @UseGuards(JwtAuthGuard)
  async findAll(@CurrentUser() user: AuthenticatedUser) {
    const currentUser = this.requireUser(user);
    const data = await this.intentionService.findAll(currentUser.userId);
    return {
      success: true,
      message: 'Intentions retrieved successfully',
      data,
    };
  }

  @ApiOperation({ summary: 'Create a new custom intention' })
  @Post()
  @UseGuards(JwtAuthGuard)
  async create(
    @CurrentUser() user: AuthenticatedUser,
    @Body('label') label: string,
  ) {
    const currentUser = this.requireUser(user);
    const data = await this.intentionService.create(currentUser.userId, label);
    return {
      success: true,
      message: 'Intention created successfully',
      data,
    };
  }

  @ApiOperation({ summary: 'Delete a custom intention' })
  @Delete(':id')
  @UseGuards(JwtAuthGuard)
  async delete(
    @CurrentUser() user: AuthenticatedUser,
    @Param('id') id: string,
  ) {
    const currentUser = this.requireUser(user);
    await this.intentionService.delete(currentUser.userId, id);
    return {
      success: true,
      message: 'Intention deleted successfully',
    };
  }
}
