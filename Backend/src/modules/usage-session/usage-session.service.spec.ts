import { UsageSessionService } from './usage-session.service';

describe('UsageSessionService', () => {
  const bulkWrite = jest.fn();
  const exec = jest.fn();
  const sort = jest.fn(() => ({ exec }));
  const find = jest.fn(() => ({ sort }));
  const usageSessionModel = {
    bulkWrite,
    find,
  };
  const deviceService = {
    markSynced: jest.fn(),
    resolveAnonymousUserIdForDevice: jest.fn(),
  };

  let service: UsageSessionService;

  beforeEach(() => {
    jest.clearAllMocks();
    service = new UsageSessionService(
      usageSessionModel as never,
      deviceService as never,
    );
  });

  it('deduplicates sync payloads by externalId before writing', async () => {
    bulkWrite.mockResolvedValue({
      matchedCount: 0,
      modifiedCount: 0,
      upsertedCount: 1,
    });
    deviceService.markSynced.mockResolvedValue(undefined);
    deviceService.resolveAnonymousUserIdForDevice.mockResolvedValue('anon-1');

    const result = await service.sync('user-1', {
      sessions: [
        {
          appName: 'YouTube',
          deviceId: 'device-1',
          deviceLocalDate: '2026-05-10',
          durationSeconds: 60,
          endedAt: '2026-05-10T00:01:00.000Z',
          externalId: 'session-1',
          packageName: 'com.google.android.youtube',
          startedAt: '2026-05-10T00:00:00.000Z',
        },
        {
          appName: 'YouTube',
          deviceId: 'device-1',
          deviceLocalDate: '2026-05-10',
          durationSeconds: 120,
          endedAt: '2026-05-10T00:02:00.000Z',
          externalId: 'session-1',
          packageName: 'com.google.android.youtube',
          startedAt: '2026-05-10T00:00:00.000Z',
        },
      ],
    });

    expect(bulkWrite).toHaveBeenCalledTimes(1);
    expect(bulkWrite.mock.calls[0][0]).toHaveLength(1);
    expect(deviceService.markSynced).toHaveBeenCalledWith('device-1', {
      anonymousUserId: 'anon-1',
      userId: 'user-1',
    });
    expect(result).toEqual({
      insertedCount: 1,
      matchedCount: 0,
      processedCount: 1,
      updatedCount: 0,
    });
  });

  it('adds anonymousUserId to date-range queries when provided', async () => {
    exec.mockResolvedValue([]);

    await service.findByDateRange(
      'anon-1',
      'device-1',
      '2026-05-04',
      '2026-05-10',
    );

    expect(find).toHaveBeenCalledWith({
      anonymousUserId: 'anon-1',
      clientDeviceId: 'device-1',
      deviceLocalDate: {
        $gte: '2026-05-04',
        $lte: '2026-05-10',
      },
    });
  });
});
