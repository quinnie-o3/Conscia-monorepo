import { BadRequestException } from '@nestjs/common';

import { StatsService } from './stats.service';

describe('StatsService', () => {
  const aggregateExec = jest.fn();
  const usageSessionModel = {
    aggregate: jest.fn(() => ({
      exec: aggregateExec,
    })),
  };
  const trackingRuleModel = {
    collection: {
      name: 'trackingrules',
    },
  };
  const usageSessionService = {
    findByDate: jest.fn(),
  };
  const trackingRuleService = {
    findActiveRules: jest.fn(),
  };

  let service: StatsService;

  beforeEach(() => {
    jest.clearAllMocks();
    service = new StatsService(
      usageSessionModel as never,
      trackingRuleModel as never,
      usageSessionService as never,
      trackingRuleService as never,
    );
  });

  it('aggregates usage-by-purpose into the Android insight response shape', async () => {
    aggregateExec.mockResolvedValue([
      {
        details: [
          {
            category: 'TRACKED',
            duration: 5400,
            tagName: 'Learning',
          },
          {
            category: 'OTHER',
            duration: 600,
            tagName: 'Other',
          },
        ],
        apps: [
          {
            appName: 'YouTube',
            packageName: 'com.google.android.youtube',
            totalDurationSeconds: 5400,
          },
          {
            appName: 'Chrome',
            packageName: 'com.android.chrome',
            totalDurationSeconds: 600,
          },
        ],
        summary: [
          {
            totalSeconds: 6000,
            trackedSeconds: 5400,
          },
        ],
        topTrackedApp: [
          {
            appName: 'YouTube',
            packageName: 'com.google.android.youtube',
            totalDurationSeconds: 5400,
          },
        ],
        trackedPackages: [
          {
            packages: ['com.google.android.youtube'],
          },
        ],
      },
    ]);
    trackingRuleService.findActiveRules.mockResolvedValue([
      { packageName: 'com.google.android.youtube', trackingEnabled: true },
      { packageName: 'com.example.docs', trackingEnabled: true },
    ]);

    const result = await service.getUsageByPurpose(
      undefined,
      'device-1',
      '2026-05-04',
      '2026-05-10',
      undefined,
      undefined,
    );

    expect(usageSessionModel.aggregate).toHaveBeenCalledTimes(1);
    expect(trackingRuleService.findActiveRules).toHaveBeenCalledWith(
      undefined,
      'device-1',
    );
    expect(result).toEqual({
      apps: [
        {
          appName: 'YouTube',
          packageName: 'com.google.android.youtube',
          percentage: 90,
          totalDurationSeconds: 5400,
        },
        {
          appName: 'Chrome',
          packageName: 'com.android.chrome',
          percentage: 10,
          totalDurationSeconds: 600,
        },
      ],
      details: [
        {
          category: 'TRACKED',
          colorCode: '#006654',
          duration: 5400,
          percentage: 90,
          tagName: 'Learning',
        },
        {
          category: 'OTHER',
          colorCode: '#999999',
          duration: 600,
          percentage: 10,
          tagName: 'Other',
        },
      ],
      range: {
        dayCount: 7,
        from: '2026-05-04',
        to: '2026-05-10',
      },
      summary: {
        averageDailySeconds: 857,
        distractingPercentage: 10,
        otherSeconds: 600,
        purposefulPercentage: 90,
        totalSeconds: 6000,
        trackedAppsCount: 2,
        trackedSeconds: 5400,
      },
      topTrackedApp: {
        appName: 'YouTube',
        packageName: 'com.google.android.youtube',
        totalDurationSeconds: 5400,
      },
    });
  });

  it('rejects invalid timezone values when the client asks for a relative period', async () => {
    await expect(
      service.getUsageByPurpose(
        undefined,
        'device-1',
        undefined,
        undefined,
        '7d',
        undefined,
        'Invalid/Timezone',
      ),
    ).rejects.toBeInstanceOf(BadRequestException);
  });

  it('returns zero usage for tracked apps missing from the current daily sessions', async () => {
    usageSessionService.findByDate.mockResolvedValue([]);
    trackingRuleService.findActiveRules.mockResolvedValue([
      {
        appName: 'YouTube',
        dailyLimitMinutes: 30,
        intentionLabel: 'Study',
        packageName: 'com.google.android.youtube',
        trackingEnabled: true,
        warningEnabled: true,
      },
    ]);

    const result = await service.getDailySummary(
      undefined,
      'device-1',
      '2026-05-26',
    );

    expect(usageSessionService.findByDate).toHaveBeenCalledWith(
      undefined,
      'device-1',
      '2026-05-26',
      undefined,
    );
    expect(result).toEqual({
      byApp: [
        {
          appName: 'YouTube',
          durationSeconds: 0,
          isExceeded: false,
          limitMinutes: 30,
          packageName: 'com.google.android.youtube',
          purposeTag: 'Study',
          usedMinutes: 0,
        },
      ],
      byPurpose: [],
      date: '2026-05-26',
      limitWarnings: [],
      totalDurationSeconds: 0,
      totalTrackedMinutes: 0,
      totalUsedMinutes: 0,
    });
  });
});
