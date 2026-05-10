import { TrackingRuleService } from './tracking-rule.service';

describe('TrackingRuleService', () => {
  const findOneAndUpdate = jest.fn();
  const deleteOne = jest.fn();
  const find = jest.fn();
  const trackingRuleModel = {
    deleteOne,
    find,
    findOneAndUpdate,
  };

  let service: TrackingRuleService;

  beforeEach(() => {
    jest.clearAllMocks();
    service = new TrackingRuleService(trackingRuleModel as never);
  });

  it('uses the resolved anonymous identity in rule upserts', async () => {
    findOneAndUpdate.mockResolvedValue({});

    await service.upsert({
      appName: 'YouTube',
      dailyLimitMinutes: 60,
      deviceId: 'device-1',
      packageName: 'com.google.android.youtube',
    });

    expect(findOneAndUpdate).toHaveBeenCalledWith(
      {
        anonymousUserId: 'device-1',
        deviceId: 'device-1',
        packageName: 'com.google.android.youtube',
      },
      {
        $set: {
          anonymousUserId: 'device-1',
          appName: 'YouTube',
          dailyLimitMinutes: 60,
          deviceId: 'device-1',
          intentionLabel: undefined,
          packageName: 'com.google.android.youtube',
          purposeTag: undefined,
          trackingEnabled: true,
          warningEnabled: true,
        },
      },
      {
        new: true,
        upsert: true,
      },
    );
  });

  it('filters active rules by anonymousUserId when one is provided', async () => {
    find.mockResolvedValue([]);

    await service.findActiveRules('anon-1', 'device-1');

    expect(find).toHaveBeenCalledWith({
      anonymousUserId: 'anon-1',
      deviceId: 'device-1',
      trackingEnabled: true,
    });
  });
});
