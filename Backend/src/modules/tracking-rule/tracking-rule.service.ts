import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';

import { normalizeOptionalString } from '../../common/device-identity.util';
import { DeviceService } from '../device/device.service';
import { TrackingRule } from './tracking-rule.schema';
import { UpsertTrackingRuleDto } from './dto/upsert-tracking-rule.dto';

@Injectable()
export class TrackingRuleService {
  constructor(
    @InjectModel(TrackingRule.name)
    private readonly trackingRuleModel: Model<TrackingRule>,
    private readonly deviceService: DeviceService,
  ) {}

  private buildAnonymousIdentityFilter(
    anonymousUserId: string | undefined,
    deviceId: string,
  ) {
    const filter: Record<string, unknown> = {
      deviceId,
    };
    const normalizedAnonymousUserId = normalizeOptionalString(anonymousUserId);

    if (normalizedAnonymousUserId) {
      filter.anonymousUserId = normalizedAnonymousUserId;
    }

    return filter;
  }

  private buildUserIdentityFilter(userId: string, deviceId: string) {
    return {
      deviceId,
      userId,
    };
  }

  async upsert(userId: string, dto: UpsertTrackingRuleDto) {
    const anonymousUserId =
      await this.deviceService.resolveAnonymousUserIdForDevice(
        dto.deviceId,
      );

    await this.deviceService.attachUser(
      dto.deviceId,
      userId,
      anonymousUserId,
    );

    return this.trackingRuleModel.findOneAndUpdate(
      {
        userId,
        packageName: dto.packageName,
      },
      {
        $set: {
          anonymousUserId,
          appName: dto.appName,
          deviceId: dto.deviceId,
          purposeTag: dto.purposeTag,
          intentionLabel: dto.intentionLabel,
          dailyLimitMinutes: dto.dailyLimitMinutes,
          packageName: dto.packageName,
          trackingEnabled: dto.trackingEnabled ?? true,
          userId,
          warningEnabled: dto.warningEnabled ?? true,
          extensionMinutes: dto.extensionMinutes ?? 0,
          extensionCount: dto.extensionCount ?? 0,
          lastExtensionDate: dto.lastExtensionDate,
        },
      },
      {
        upsert: true,
        new: true,
      },
    ).exec();
  }

  async findAllForUser(userId: string, deviceId: string) {
    return this.trackingRuleModel
      .find({ userId }) // Rules follow user, but we might still want to filter by device if needed
      .sort({ createdAt: -1 })
      .exec();
  }

  async deleteOneForUser(
    userId: string,
    deviceId: string,
    packageName: string,
  ) {
    return this.trackingRuleModel
      .deleteOne({
        userId,
        packageName,
      })
      .exec();
  }

  async findActiveRules(
    anonymousUserId: string | undefined,
    deviceId: string,
  ) {
    return this.trackingRuleModel
      .find({
        ...this.buildAnonymousIdentityFilter(anonymousUserId, deviceId),
        trackingEnabled: true,
      })
      .exec();
  }
}
