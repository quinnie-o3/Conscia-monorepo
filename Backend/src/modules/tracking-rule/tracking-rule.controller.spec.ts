import { TrackingRuleController } from './tracking-rule.controller';

describe('TrackingRuleController', () => {
  const trackingRuleService = {
    deleteOne: jest.fn(),
    findAll: jest.fn(),
    upsert: jest.fn(),
  };

  let controller: TrackingRuleController;

  beforeEach(() => {
    jest.clearAllMocks();
    controller = new TrackingRuleController(trackingRuleService as never);
  });

  it('wraps saved rules in the standard API envelope', async () => {
    const rule = {
      appName: 'YouTube',
      packageName: 'com.google.android.youtube',
    };
    trackingRuleService.upsert.mockResolvedValue(rule);

    await expect(
      controller.upsert(
        { userId: 'user-1' },
        {
          appName: 'YouTube',
          dailyLimitMinutes: 60,
          deviceId: 'device-1',
          packageName: 'com.google.android.youtube',
        },
      ),
    ).resolves.toEqual({
      success: true,
      message: 'Tracking rule saved successfully',
      data: rule,
    });
  });
});
