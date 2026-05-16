import { Body, Controller, Get, Post } from '@nestjs/common';
import {
  ApiBody,
  ApiOperation,
  ApiResponse,
  ApiTags,
} from '@nestjs/swagger';
import { RegisterDeviceDto } from './dto/register-device.dto';
import { DeviceService } from './device.service';

@ApiTags('Devices')
@Controller('devices')
export class DeviceController {
  constructor(private readonly deviceService: DeviceService) {}

  @ApiOperation({ summary: 'Register or update a device' })
  @ApiBody({ type: RegisterDeviceDto })
  @ApiResponse({
    status: 201,
    description: 'Device registered or updated successfully.',
  })
  @ApiResponse({
    status: 400,
    description: 'Request body validation failed.',
  })
  @Post('register')
  async register(@Body() dto: RegisterDeviceDto) {
    const device = await this.deviceService.register(dto);

    return {
      success: true,
      message: 'Device registered successfully',
      data: device,
    };
  }

  @ApiOperation({ summary: 'List all registered devices' })
  @ApiResponse({
    status: 200,
    description: 'Devices retrieved successfully.',
  })
  @Get()
  async findAll() {
    const devices = await this.deviceService.findAll();

    return {
      success: true,
      message: 'Devices retrieved successfully',
      data: devices,
    };
  }
}
