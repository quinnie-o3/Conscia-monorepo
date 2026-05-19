import { TrackingRuleService } from './tracking-rule.service';

describe('TrackingRuleService', () => {
  const findOneAndUpdate = jest.fn();
  const deleteOne = jest.fn();
  const find = jest.fn();
  const resolveAnonymousUserIdForDevice = jest.fn();
  const attachUser = jest.fn();
  const trackingRuleModel = {
    deleteOne,
    find,
    findOneAndUpdate,
  };
  const deviceService = {
    attachUser,
    resolveAnonymousUserIdForDevice,
  };

  let service: TrackingRuleService;

  beforeEach(() => {
    jest.clearAllMocks();
    resolveAnonymousUserIdForDevice.mockResolvedValue('anon-1');
    attachUser.mockResolvedValue({});
    service = new TrackingRuleService(
      trackingRuleModel as never,
      deviceService as never,
    );
  });

  it('uses the user and resolved anonymous identity in rule upserts', async () => {
    findOneAndUpdate.mockResolvedValue({});

    await service.upsert('user-1', {
      appName: 'YouTube',
      dailyLimitMinutes: 60,
      deviceId: 'device-1',
      packageName: 'com.google.android.youtube',
    } as never);

    expect(resolveAnonymousUserIdForDevice).toHaveBeenCalledWith('device-1');
    expect(attachUser).toHaveBeenCalledWith('device-1', 'user-1', 'anon-1');

    expect(findOneAndUpdate).toHaveBeenCalledWith(
      {
        userId: 'user-1',
        packageName: 'com.google.android.youtube',
      },
      {
        $set: {
          anonymousUserId: 'anon-1',
          appName: 'YouTube',
          dailyLimitMinutes: 60,
          deviceId: 'device-1',
          extensionCount: 0,
          extensionMinutes: 0,
          intentionLabel: undefined,
          lastExtensionDate: undefined,
          packageName: 'com.google.android.youtube',
          purposeTag: undefined,
          trackingEnabled: true,
          userId: 'user-1',
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
