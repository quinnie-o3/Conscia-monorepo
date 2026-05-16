import { BadRequestException, Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';

import { normalizeOptionalString } from '../../common/device-identity.util';
import { DeviceService } from '../device/device.service';
import {
  SyncUsageSessionsDto,
  UsageSessionItemDto,
} from './dto/sync-usage-sessions.dto';
import { UsageSession } from './usage-session.schema';

type NormalizedUsageSession = {
  anonymousUserId: string;
  appName: string;
  clientDeviceId: string;
  dailyLimitMinutes?: number;
  deviceId: string;
  deviceLocalDate: string;
  deviceTimezone?: string;
  durationSeconds: number;
  endedAt: Date;
  externalId: string;
  intentionLabel?: string;
  isClassified: boolean;
  packageName: string;
  purposeTag?: string;
  startedAt: Date;
  tags: string[];
  timezoneOffsetMinutes?: number;
  trackingEnabled: boolean;
  userId: string;
  warningEnabled: boolean;
};

@Injectable()
export class UsageSessionService {
  constructor(
    @InjectModel(UsageSession.name)
    private readonly usageSessionModel: Model<UsageSession>,
    private readonly deviceService: DeviceService,
  ) {}

  private buildIdentityFilter(
    anonymousUserId: string | undefined,
    deviceId: string,
    userId?: string,
  ) {
    if (userId) {
      return { userId };
    }

    const filter: Record<string, unknown> = {
      clientDeviceId: deviceId,
    };
    const normalizedAnonymousUserId = normalizeOptionalString(anonymousUserId);

    if (normalizedAnonymousUserId) {
      filter.anonymousUserId = normalizedAnonymousUserId;
    }

    return filter;
  }

  private normalizeRequiredString(
    value: string | undefined,
    fieldName: string,
  ) {
    const normalizedValue = value?.trim();

    if (!normalizedValue) {
      throw new BadRequestException(`${fieldName} is required`);
    }

    return normalizedValue;
  }

  private normalizeRequiredDateOnly(
    value: string | undefined,
    fieldName: string,
  ) {
    const normalizedValue = this.normalizeRequiredString(value, fieldName);

    if (!/^\d{4}-\d{2}-\d{2}$/.test(normalizedValue)) {
      throw new BadRequestException(
        `${fieldName} must be in yyyy-MM-dd format`,
      );
    }

    return normalizedValue;
  }

  private parseRequiredDate(value: string | undefined, fieldName: string) {
    const normalizedValue = this.normalizeRequiredString(value, fieldName);
    const parsedDate = new Date(normalizedValue);

    if (Number.isNaN(parsedDate.getTime())) {
      throw new BadRequestException(`${fieldName} must be a valid ISO date`);
    }

    return parsedDate;
  }

  private normalizeTags(tags: string[] | undefined) {
    return (tags || [])
      .map((tag) => tag.trim())
      .filter((tag) => tag.length > 0);
  }

  private async normalizeSession(
    userId: string,
    dto: SyncUsageSessionsDto,
    session: UsageSessionItemDto,
  ): Promise<NormalizedUsageSession> {
    const deviceId = this.normalizeRequiredString(
      session.deviceId || dto.deviceId,
      'deviceId',
    );
    const packageName = this.normalizeRequiredString(
      session.packageName,
      'packageName',
    );
    const deviceLocalDate = this.normalizeRequiredDateOnly(
      session.deviceLocalDate,
      'deviceLocalDate',
    );
    const startedAt = this.parseRequiredDate(
      session.startedAt || session.startTime,
      'startedAt',
    );
    const endedAt = this.parseRequiredDate(
      session.endedAt || session.endTime,
      'endedAt',
    );

    if (endedAt < startedAt) {
      throw new BadRequestException('endedAt must be after startedAt');
    }

    const appName = normalizeOptionalString(session.appName) || packageName;
    const purposeTag = normalizeOptionalString(session.purposeTag);
    const intentionLabel = normalizeOptionalString(session.intentionLabel);
    const tags = this.normalizeTags(session.tags);
    const anonymousUserId =
      await this.deviceService.resolveAnonymousUserIdForDevice(deviceId);
    const externalId =
      normalizeOptionalString(session.externalId) ||
      `${anonymousUserId}:${deviceId}:${packageName}:${deviceLocalDate}`;

    return {
      anonymousUserId,
      appName,
      clientDeviceId: deviceId,
      dailyLimitMinutes: session.dailyLimitMinutes ?? undefined,
      deviceId,
      deviceLocalDate,
      deviceTimezone: normalizeOptionalString(session.deviceTimezone),
      durationSeconds: session.durationSeconds,
      endedAt,
      externalId,
      intentionLabel,
      isClassified:
        session.isClassified ?? (tags.length > 0 || Boolean(intentionLabel)),
      packageName,
      purposeTag,
      startedAt,
      tags,
      timezoneOffsetMinutes: session.timezoneOffsetMinutes ?? undefined,
      trackingEnabled: session.trackingEnabled ?? false,
      userId,
      warningEnabled: session.warningEnabled ?? false,
    };
  }

  async sync(userId: string, dto: SyncUsageSessionsDto) {
    const sessionsByExternalId = new Map<string, NormalizedUsageSession>();
    const deviceIdentityById = new Map<
      string,
      { anonymousUserId: string; userId: string }
    >();

    for (const session of dto.sessions) {
      const normalizedSession = await this.normalizeSession(
        userId,
        dto,
        session,
      );

      sessionsByExternalId.set(normalizedSession.externalId, normalizedSession);
      deviceIdentityById.set(normalizedSession.deviceId, {
        anonymousUserId: normalizedSession.anonymousUserId,
        userId,
      });
    }

    const normalizedSessions = Array.from(sessionsByExternalId.values());

    if (normalizedSessions.length === 0) {
      return {
        processedCount: 0,
        insertedCount: 0,
        updatedCount: 0,
        matchedCount: 0,
      };
    }

    const result = await this.usageSessionModel.bulkWrite(
      normalizedSessions.map((session) => ({
        updateOne: {
          filter: { externalId: session.externalId },
          update: { $set: session },
          upsert: true,
        },
      })),
      { ordered: false },
    );

    await Promise.all(
      Array.from(deviceIdentityById.entries()).map(
        ([deviceId, identity]) =>
          this.deviceService.markSynced(deviceId, identity),
      ),
    );

    return {
      processedCount: normalizedSessions.length,
      insertedCount: result.upsertedCount,
      updatedCount: result.modifiedCount,
      matchedCount: result.matchedCount,
    };
  }

  async findByDate(
    anonymousUserId: string | undefined,
    deviceId: string,
    date: string,
    userId?: string,
  ) {
    return this.usageSessionModel
      .find({
        ...this.buildIdentityFilter(anonymousUserId, deviceId, userId),
        deviceLocalDate: date,
      })
      .sort({ startedAt: 1, packageName: 1 })
      .exec();
  }

  async findByDateRange(
    anonymousUserId: string | undefined,
    deviceId: string,
    from: string,
    to: string,
    userId?: string,
  ) {
    return this.usageSessionModel
      .find({
        ...this.buildIdentityFilter(anonymousUserId, deviceId, userId),
        deviceLocalDate: {
          $gte: from,
          $lte: to,
        },
      })
      .sort({ deviceLocalDate: 1, startedAt: 1, packageName: 1 })
      .exec();
  }
}
