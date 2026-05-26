import { BadRequestException, Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, PipelineStage } from 'mongoose';

import {
  formatDateOnly,
  getCurrentDateOnly,
  parseDateOnly,
} from '../../common/date-only.util';
import { normalizeOptionalString } from '../../common/device-identity.util';
import { TrackingRuleService } from '../tracking-rule/tracking-rule.service';
import { TrackingRule } from '../tracking-rule/tracking-rule.schema';
import { UsageSessionService } from '../usage-session/usage-session.service';
import { UsageSession } from '../usage-session/usage-session.schema';

type UsageCategory = 'TRACKED' | 'OTHER';

type RuleContext = {
  appName?: string;
  dailyLimitMinutes?: number;
  intentionLabel?: string;
  packageName: string;
  purposeTag?: string;
  trackingEnabled?: boolean;
  warningEnabled?: boolean;
};

type ResolvedRange = {
  dayCount: number;
  from: string;
  to: string;
};

@Injectable()
export class StatsService {
  private readonly trackedColorByTag: Record<string, string> = {
    communication: '#2563EB',
    creativity: '#7C3AED',
    entertainment: '#6391F2',
    focus: '#0F766E',
    health: '#0EA5E9',
    learning: '#006654',
    productivity: '#0F766E',
    social: '#C2410C',
    study: '#006654',
    tracked: '#006654',
    work: '#1D4ED8',
  };

  constructor(
    @InjectModel(UsageSession.name)
    private readonly usageSessionModel: Model<UsageSession>,
    @InjectModel(TrackingRule.name)
    private readonly trackingRuleModel: Model<TrackingRule>,
    private readonly usageSessionService: UsageSessionService,
    private readonly trackingRuleService: TrackingRuleService,
  ) {}

  private roundPercentage(part: number, total: number) {
    if (total <= 0) {
      return 0;
    }

    return Number(((part / total) * 100).toFixed(1));
  }

  private getPeriodRange(
    period: string | undefined,
    anchorDate: string | undefined,
    timeZone: string | undefined,
  ) {
    const normalizedPeriod = (period || '7d').trim().toLowerCase();
    let dayCount = 7;

    if (
      normalizedPeriod === '1d' ||
      normalizedPeriod === 'day' ||
      normalizedPeriod === 'daily'
    ) {
      dayCount = 1;
    } else if (
      normalizedPeriod === '7d' ||
      normalizedPeriod === 'week' ||
      normalizedPeriod === 'weekly'
    ) {
      dayCount = 7;
    } else if (
      normalizedPeriod === '30d' ||
      normalizedPeriod === 'month' ||
      normalizedPeriod === 'monthly'
    ) {
      dayCount = 30;
    } else if (/^\d+d$/.test(normalizedPeriod)) {
      dayCount = Number.parseInt(normalizedPeriod.slice(0, -1), 10);
    } else {
      throw new BadRequestException(
        'period must be one of: 1d, 7d, 30d, day, week, month',
      );
    }

    const end = anchorDate
      ? parseDateOnly(anchorDate)
      : parseDateOnly(this.resolveCurrentDateOnly(timeZone));

    if (Number.isNaN(end.getTime())) {
      throw new BadRequestException('date must be in yyyy-MM-dd format');
    }

    const start = new Date(end);
    start.setDate(start.getDate() - (dayCount - 1));

    return {
      dayCount,
      from: formatDateOnly(start),
      to: formatDateOnly(end),
    };
  }

  private resolveRange(
    from: string | undefined,
    to: string | undefined,
    period: string | undefined,
    anchorDate: string | undefined,
    timeZone: string | undefined,
  ): ResolvedRange {
    if (from || to) {
      if (!from || !to) {
        throw new BadRequestException('from and to are required together');
      }

      const start = parseDateOnly(from);
      const end = parseDateOnly(to);

      if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
        throw new BadRequestException(
          'from and to must be in yyyy-MM-dd format',
        );
      }

      if (start > end) {
        throw new BadRequestException('from must be before or equal to to');
      }

      const dayCount =
        Math.floor((end.getTime() - start.getTime()) / (24 * 60 * 60 * 1000)) +
        1;

      return {
        dayCount,
        from,
        to,
      };
    }

    return this.getPeriodRange(period, anchorDate, timeZone);
  }

  private buildRulesByPackage(rules: RuleContext[]) {
    return new Map(rules.map((rule) => [rule.packageName, rule]));
  }

  private resolvePurposeLabel(
    session: {
      intentionLabel?: string;
      packageName: string;
      purposeTag?: string;
    },
    rule?: RuleContext,
  ) {
    return (
      normalizeOptionalString(session.intentionLabel) ||
      normalizeOptionalString(rule?.intentionLabel) ||
      normalizeOptionalString(session.purposeTag) ||
      normalizeOptionalString(rule?.purposeTag)
    );
  }

  private resolveUsageCategory(
    session: {
      intentionLabel?: string;
      packageName: string;
      purposeTag?: string;
      trackingEnabled?: boolean;
    },
    rule?: RuleContext,
  ): UsageCategory {
    const hasPurposeLabel = Boolean(this.resolvePurposeLabel(session, rule));
    const isTracked =
      Boolean(session.trackingEnabled) ||
      Boolean(rule?.trackingEnabled) ||
      hasPurposeLabel;

    return isTracked ? 'TRACKED' : 'OTHER';
  }

  private resolveTagName(
    session: {
      intentionLabel?: string;
      packageName: string;
      purposeTag?: string;
    },
    rule: RuleContext | undefined,
    category: UsageCategory,
  ) {
    return (
      this.resolvePurposeLabel(session, rule) ||
      (category === 'TRACKED' ? 'Tracked' : 'Other')
    );
  }

  private resolveColorCode(tagName: string, category: UsageCategory) {
    if (category === 'OTHER') {
      return '#999999';
    }

    return this.trackedColorByTag[tagName.trim().toLowerCase()] || '#006654';
  }

  private resolveCurrentDateOnly(timeZone: string | undefined) {
    try {
      return getCurrentDateOnly(normalizeOptionalString(timeZone));
    } catch {
      throw new BadRequestException(
        'timezone must be a valid IANA timezone, for example Asia/Saigon',
      );
    }
  }

  private buildOptionalTrimmedStringExpression(path: string) {
    return {
      $let: {
        vars: {
          value: {
            $trim: {
              input: { $ifNull: [path, ''] },
            },
          },
        },
        in: {
          $cond: [{ $eq: ['$$value', ''] }, null, '$$value'],
        },
      },
    };
  }

  private buildUsageByPurposePipeline(
    anonymousUserId: string | undefined,
    deviceId: string,
    from: string,
    to: string,
    userId?: string,
  ): PipelineStage[] {
    const matchStage: PipelineStage.Match['$match'] = {
      deviceLocalDate: {
        $gte: from,
        $lte: to,
      },
    };

    if (userId) {
      matchStage.userId = userId;
    } else {
      matchStage.clientDeviceId = deviceId;
      const normalizedAnonymousUserId = normalizeOptionalString(anonymousUserId);
      if (normalizedAnonymousUserId) {
        matchStage.anonymousUserId = normalizedAnonymousUserId;
      }
    }

    const sessionIntentionLabel =
      this.buildOptionalTrimmedStringExpression('$intentionLabel');
    const ruleIntentionLabel = this.buildOptionalTrimmedStringExpression(
      '$rule.intentionLabel',
    );
    const sessionPurposeTag =
      this.buildOptionalTrimmedStringExpression('$purposeTag');
    const rulePurposeTag =
      this.buildOptionalTrimmedStringExpression('$rule.purposeTag');
    const sessionAppName =
      this.buildOptionalTrimmedStringExpression('$appName');
    const ruleAppName =
      this.buildOptionalTrimmedStringExpression('$rule.appName');
    const resolvedPurposeLabel = {
      $ifNull: [
        sessionIntentionLabel,
        {
          $ifNull: [
            ruleIntentionLabel,
            {
              $ifNull: [sessionPurposeTag, rulePurposeTag],
            },
          ],
        },
      ],
    };

    return [
      {
        $match: matchStage,
      },
      {
        $lookup: {
          from: this.trackingRuleModel.collection.name,
          let: {
            sessionUserId: '$userId',
            sessionDeviceId: '$clientDeviceId',
            sessionPackageName: '$packageName',
          },
          pipeline: [
            {
              $match: {
                $expr: {
                  $and: [
                    {
                      $cond: [
                        { $ne: ['$$sessionUserId', null] },
                        { $eq: ['$userId', '$$sessionUserId'] },
                        { $eq: ['$deviceId', '$$sessionDeviceId'] }
                      ]
                    },
                    { $eq: ['$packageName', '$$sessionPackageName'] },
                    { $eq: ['$trackingEnabled', true] },
                  ],
                },
              },
            },
            {
              $project: {
                _id: 0,
                appName: 1,
                intentionLabel: 1,
                packageName: 1,
                purposeTag: 1,
                trackingEnabled: 1,
              },
            },
            { $limit: 1 },
          ],
          as: 'rule',
        },
      },
      {
        $set: {
          rule: { $arrayElemAt: ['$rule', 0] },
        },
      },
      {
        $set: {
          isTracked: {
            $or: [
              { $eq: ['$trackingEnabled', true] },
              { $eq: ['$rule.trackingEnabled', true] },
              { $ne: [resolvedPurposeLabel, null] },
            ],
          },
          resolvedAppName: {
            $ifNull: [
              sessionAppName,
              { $ifNull: [ruleAppName, '$packageName'] },
            ],
          },
          resolvedPurposeLabel,
        },
      },
      {
        $set: {
          category: {
            $cond: ['$isTracked', 'TRACKED', 'OTHER'],
          },
          tagName: {
            $ifNull: [
              '$resolvedPurposeLabel',
              {
                $cond: ['$isTracked', 'Tracked', 'Other'],
              },
            ],
          },
        },
      },
      {
        $facet: {
          details: [
            {
              $group: {
                _id: {
                  category: '$category',
                  tagName: '$tagName',
                },
                duration: { $sum: '$durationSeconds' },
              },
            },
            {
              $match: {
                duration: { $gt: 0 },
              },
            },
            {
              $project: {
                _id: 0,
                category: '$_id.category',
                duration: 1,
                tagName: '$_id.tagName',
              },
            },
            {
              $sort: {
                duration: -1,
                tagName: 1,
              },
            },
          ],
          apps: [
            {
              $group: {
                _id: {
                  appName: '$resolvedAppName',
                  packageName: '$packageName',
                },
                totalDurationSeconds: { $sum: '$durationSeconds' },
              },
            },
            {
              $match: {
                totalDurationSeconds: { $gt: 0 },
              },
            },
            {
              $sort: {
                totalDurationSeconds: -1,
                '_id.appName': 1,
                '_id.packageName': 1,
              },
            },
            {
              $project: {
                _id: 0,
                appName: '$_id.appName',
                packageName: '$_id.packageName',
                totalDurationSeconds: 1,
              },
            },
          ],
          summary: [
            {
              $group: {
                _id: null,
                totalSeconds: { $sum: '$durationSeconds' },
                trackedSeconds: {
                  $sum: {
                    $cond: ['$isTracked', '$durationSeconds', 0],
                  },
                },
              },
            },
            {
              $project: {
                _id: 0,
                totalSeconds: 1,
                trackedSeconds: 1,
              },
            },
          ],
          trackedPackages: [
            {
              $match: {
                isTracked: true,
              },
            },
            {
              $group: {
                _id: null,
                packages: { $addToSet: '$packageName' },
              },
            },
            {
              $project: {
                _id: 0,
                packages: 1,
              },
            },
          ],
          topTrackedApp: [
            {
              $match: {
                isTracked: true,
              },
            },
            {
              $group: {
                _id: {
                  appName: '$resolvedAppName',
                  packageName: '$packageName',
                },
                totalDurationSeconds: { $sum: '$durationSeconds' },
              },
            },
            {
              $sort: {
                totalDurationSeconds: -1,
                '_id.packageName': 1,
              },
            },
            { $limit: 1 },
            {
              $project: {
                _id: 0,
                appName: '$_id.appName',
                packageName: '$_id.packageName',
                totalDurationSeconds: 1,
              },
            },
          ],
        },
      },
    ];
  }

  async getDailySummary(
    anonymousUserId: string | undefined,
    deviceId: string,
    date: string,
    userId?: string,
  ) {
    const sessions = await this.usageSessionService.findByDate(
      anonymousUserId,
      deviceId,
      date,
      userId,
    );
    const rules = userId
      ? await this.trackingRuleService.findAllForUser(userId, deviceId)
      : await this.trackingRuleService.findActiveRules(
          anonymousUserId,
          deviceId,
        );
    const rulesByPackage = this.buildRulesByPackage(rules);

    const totalDurationSeconds = sessions.reduce(
      (sum, session) => sum + (session.durationSeconds || 0),
      0,
    );
    let totalTrackedSeconds = 0;
    const byPurposeMap = new Map<string, number>();
    const byAppMap = new Map<
      string,
      {
        appName?: string;
        durationSeconds: number;
        limitMinutes: number | null;
        packageName: string;
        purposeTag: string;
        warningEnabled: boolean;
      }
    >();

    for (const session of sessions) {
      const rule = rulesByPackage.get(session.packageName);
      const category = this.resolveUsageCategory(session, rule);
      const purposeTag = this.resolveTagName(session, rule, category);
      const durationSeconds = session.durationSeconds || 0;

      if (category === 'TRACKED') {
        totalTrackedSeconds += durationSeconds;
      }

      byPurposeMap.set(
        purposeTag,
        (byPurposeMap.get(purposeTag) || 0) + durationSeconds,
      );

      if (!byAppMap.has(session.packageName)) {
        byAppMap.set(session.packageName, {
          appName: session.appName,
          durationSeconds: 0,
          limitMinutes:
            session.dailyLimitMinutes ?? rule?.dailyLimitMinutes ?? null,
          packageName: session.packageName,
          purposeTag,
          warningEnabled:
            Boolean(session.warningEnabled) || Boolean(rule?.warningEnabled),
        });
      }

      const appEntry = byAppMap.get(session.packageName)!;

      appEntry.durationSeconds += durationSeconds;

      if (
        appEntry.limitMinutes === null &&
        rule?.dailyLimitMinutes !== undefined
      ) {
        appEntry.limitMinutes = rule.dailyLimitMinutes;
      }
    }

    const byPurpose = Array.from(byPurposeMap.entries())
      .filter(([, durationSeconds]) => durationSeconds > 0)
      .map(([purposeTag, durationSeconds]) => ({
        durationSeconds,
        percentage: this.roundPercentage(durationSeconds, totalDurationSeconds),
        purposeTag,
        usedMinutes: Math.round(durationSeconds / 60),
      }));

    const byApp = Array.from(byAppMap.values())
      .map((app) => {
        const usedMinutes = Math.round(app.durationSeconds / 60);
        const isExceeded =
          app.warningEnabled &&
          app.limitMinutes !== null &&
          usedMinutes > app.limitMinutes;

        return {
          appName: app.appName,
          durationSeconds: app.durationSeconds,
          isExceeded,
          limitMinutes: app.limitMinutes,
          packageName: app.packageName,
          purposeTag: app.purposeTag,
          usedMinutes,
        };
      })
      .sort((left, right) => right.durationSeconds - left.durationSeconds);

    for (const rule of rules) {
      if (!rule.trackingEnabled) {
        continue;
      }

      if (byAppMap.has(rule.packageName)) {
        continue;
      }

      byApp.push({
        appName: rule.appName,
        durationSeconds: 0,
        isExceeded: false,
        limitMinutes: rule.dailyLimitMinutes ?? null,
        packageName: rule.packageName,
        purposeTag:
          normalizeOptionalString(rule.intentionLabel) ||
          normalizeOptionalString(rule.purposeTag) ||
          'Tracked',
        usedMinutes: 0,
      });
    }

    const limitWarnings = byApp
      .filter((app) => app.isExceeded)
      .map((app) => ({
        appName: app.appName,
        exceededByMinutes:
          app.limitMinutes !== null ? app.usedMinutes - app.limitMinutes : 0,
        limitMinutes: app.limitMinutes,
        packageName: app.packageName,
        usedMinutes: app.usedMinutes,
      }));

    return {
      byApp,
      byPurpose,
      date,
      limitWarnings,
      totalDurationSeconds,
      totalUsedMinutes: Math.round(totalDurationSeconds / 60),
      totalTrackedMinutes: Math.round(totalTrackedSeconds / 60),
    };
  }

  async getUsageByPurpose(
    anonymousUserId: string | undefined,
    deviceId: string,
    from: string | undefined,
    to: string | undefined,
    period: string | undefined,
    date: string | undefined,
    timeZone?: string,
    userId?: string,
  ) {
    const range = this.resolveRange(from, to, period, date, timeZone);
    const [aggregateResult, rules] = await Promise.all([
      this.usageSessionModel
        .aggregate(
          this.buildUsageByPurposePipeline(
            anonymousUserId,
            deviceId,
            range.from,
            range.to,
            userId,
          ),
        )
        .exec(),
      userId
        ? this.trackingRuleService.findAllForUser(userId, deviceId)
        : this.trackingRuleService.findActiveRules(
            anonymousUserId,
            deviceId,
          ),
    ]);
    const result = aggregateResult[0] || {
      details: [],
      summary: [],
      topTrackedApp: [],
      trackedPackages: [],
    };
    const summary = result.summary[0] || {
      totalSeconds: 0,
      trackedSeconds: 0,
    };
    const totalSeconds = summary.totalSeconds || 0;
    const trackedSeconds = summary.trackedSeconds || 0;
    const otherSeconds = Math.max(totalSeconds - trackedSeconds, 0);
    const trackedPackagesSeen =
      result.trackedPackages[0]?.packages?.length || 0;
    const trackedRuleCount = rules.filter((rule) => rule.trackingEnabled).length;
    const details = result.details.map(
      (detail: {
        category: UsageCategory;
        duration: number;
        tagName: string;
      }) => ({
        category: detail.category,
        colorCode: this.resolveColorCode(detail.tagName, detail.category),
        duration: detail.duration,
        percentage: this.roundPercentage(detail.duration, totalSeconds),
        tagName: detail.tagName,
      }),
    );
    const apps = (result.apps || []).map(
      (app: {
        appName: string;
        packageName: string;
        totalDurationSeconds: number;
      }) => ({
        appName: app.appName,
        packageName: app.packageName,
        percentage: this.roundPercentage(app.totalDurationSeconds, totalSeconds),
        totalDurationSeconds: app.totalDurationSeconds,
      }),
    );
    const topTrackedApp = result.topTrackedApp[0] || null;

    return {
      apps,
      details,
      range: {
        dayCount: range.dayCount,
        from: range.from,
        to: range.to,
      },
      summary: {
        averageDailySeconds:
          range.dayCount > 0 ? Math.floor(totalSeconds / range.dayCount) : 0,
        distractingPercentage: this.roundPercentage(otherSeconds, totalSeconds),
        otherSeconds,
        purposefulPercentage: this.roundPercentage(
          trackedSeconds,
          totalSeconds,
        ),
        totalSeconds,
        trackedAppsCount: Math.max(trackedRuleCount, trackedPackagesSeen),
        trackedSeconds,
      },
      topTrackedApp,
    };
  }
}
