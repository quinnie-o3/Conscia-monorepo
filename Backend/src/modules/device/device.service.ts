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
          anonymousUserId,
          deviceId: dto.deviceId,
          deviceName: dto.deviceName,
          osVersion: dto.osVersion,
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

  async findByDeviceId(deviceId: string) {
    return this.deviceModel.findOne({ deviceId }).exec();
  }

  async resolveAnonymousUserIdForDevice(deviceId: string) {
    const device = await this.findByDeviceId(deviceId);

    return resolveAnonymousUserId(device?.anonymousUserId, deviceId);
  }

  async attachUser(
    deviceId: string,
    userId: string,
    anonymousUserId?: string,
  ) {
    const resolvedAnonymousUserId = resolveAnonymousUserId(
      anonymousUserId,
      deviceId,
    );

    return this.deviceModel.findOneAndUpdate(
      { deviceId },
      {
        $set: {
          anonymousUserId: resolvedAnonymousUserId,
          isActive: true,
          platform: 'android',
          userId,
        },
      },
      { upsert: true, returnDocument: 'after' },
    );
  }

  async markSynced(
    deviceId: string,
    options?: {
      anonymousUserId?: string;
      userId?: string;
    },
  ) {
    const resolvedAnonymousUserId = resolveAnonymousUserId(
      options?.anonymousUserId,
      deviceId,
    );
    const updateSet: Record<string, unknown> = {
      anonymousUserId: resolvedAnonymousUserId,
      isActive: true,
      lastSyncAt: new Date(),
      platform: 'android',
    };

    if (options?.userId) {
      updateSet.userId = options.userId;
    }

    return this.deviceModel.findOneAndUpdate(
      { deviceId },
      {
        $set: updateSet,
      },
      { upsert: true, returnDocument: 'after' },
    );
  }

  async findAll() {
    return this.deviceModel.find().sort({ updatedAt: -1 });
  }
}
