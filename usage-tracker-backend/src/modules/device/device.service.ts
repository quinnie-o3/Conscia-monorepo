import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';

import { resolveAnonymousUserId } from '../../common/device-identity.util';
import { Device } from './device.schema';
import { RegisterDeviceDto } from './dto/register-device.dto';

@Injectable()
export class DeviceService {
  constructor(
    @InjectModel(Device.name)
    private readonly deviceModel: Model<Device>,
  ) {}

  async register(dto: RegisterDeviceDto) {
    const anonymousUserId = resolveAnonymousUserId(
      dto.anonymousUserId,
      dto.deviceId,
    );

    const device = await this.deviceModel.findOneAndUpdate(
      {
        deviceId: dto.deviceId,
      },
      {
        $set: {
          ...dto,
          anonymousUserId,
          platform: 'android',
          isActive: true,
        },
      },
      {
        upsert: true,
        new: true,
      },
    );

    return device;
  }

  async markSynced(deviceId: string, anonymousUserId?: string) {
    return this.deviceModel.findOneAndUpdate(
      { deviceId },
      {
        $set: {
          platform: 'android',
          isActive: true,
          lastSyncAt: new Date(),
        },
        $setOnInsert: {
          anonymousUserId: resolveAnonymousUserId(
            anonymousUserId,
            deviceId,
          ),
        },
      },
      { upsert: true, new: true },
    );
  }

  async findAll() {
    return this.deviceModel.find().sort({ updatedAt: -1 });
  }
}
