import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';

import {
  normalizeOptionalString,
  resolveAnonymousUserId,
} from '../../common/device-identity.util';
import { TrackingRule } from './tracking-rule.schema';
import { UpsertTrackingRuleDto } from './dto/upsert-tracking-rule.dto';

@Injectable()
export class TrackingRuleService {
  constructor(
    @InjectModel(TrackingRule.name)
    private readonly trackingRuleModel: Model<TrackingRule>,
  ) {}

  private buildIdentityFilter(
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

  async upsert(dto: UpsertTrackingRuleDto) {
    const anonymousUserId = resolveAnonymousUserId(
      dto.anonymousUserId,
      dto.deviceId,
    );

    return this.trackingRuleModel.findOneAndUpdate(
      {
        anonymousUserId,
        deviceId: dto.deviceId,
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
          warningEnabled: dto.warningEnabled ?? true,
        },
      },
      {
        upsert: true,
        new: true,
      },
    );
  }

  async findAll(_anonymousUserId: string | undefined, deviceId: string) {
    return this.trackingRuleModel
      .find(this.buildIdentityFilter(_anonymousUserId, deviceId))
      .sort({ createdAt: -1 });
  }

  async deleteOne(
    _anonymousUserId: string | undefined,
    deviceId: string,
    packageName: string,
  ) {
    return this.trackingRuleModel.deleteOne({
      ...this.buildIdentityFilter(_anonymousUserId, deviceId),
      packageName,
    });
  }

  async findActiveRules(
    _anonymousUserId: string | undefined,
    deviceId: string,
  ) {
    return this.trackingRuleModel.find({
      ...this.buildIdentityFilter(_anonymousUserId, deviceId),
      trackingEnabled: true,
    });
  }
}
