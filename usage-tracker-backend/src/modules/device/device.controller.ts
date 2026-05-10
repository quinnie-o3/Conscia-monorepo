import { Body, Controller, Get, Post } from '@nestjs/common';
import { RegisterDeviceDto } from './dto/register-device.dto';
import { DeviceService } from './device.service';

@Controller('devices')
export class DeviceController {
  constructor(private readonly deviceService: DeviceService) {}

  @Post('register')
  async register(@Body() dto: RegisterDeviceDto) {
    const device = await this.deviceService.register(dto);

    return {
      success: true,
      message: 'Device registered successfully',
      data: device,
    };
  }

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
