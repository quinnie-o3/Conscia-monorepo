import { BadRequestException } from '@nestjs/common';

import { StatsController } from './stats.controller';

describe('StatsController', () => {
  const statsService = {
    getDailySummary: jest.fn(),
    getUsageByPurpose: jest.fn(),
  };

  let controller: StatsController;

  beforeEach(() => {
    jest.clearAllMocks();
    controller = new StatsController(statsService as never);
  });

  it('passes deviceId and timezone from headers when query omits them', async () => {
    statsService.getUsageByPurpose.mockResolvedValue({ details: [] });

    await controller.getUsageByPurpose(
      {
        anonymousUserId: undefined,
        date: undefined,
        deviceId: undefined,
        from: '2026-05-04',
        period: undefined,
        timezone: undefined,
        to: '2026-05-10',
      },
      { userId: 'user-1' },
      'device-from-header',
      'Asia/Saigon',
    );

    expect(statsService.getUsageByPurpose).toHaveBeenCalledWith(
      undefined,
      'device-from-header',
      '2026-05-04',
      '2026-05-10',
      undefined,
      undefined,
      'Asia/Saigon',
      'user-1',
    );
  });

  it('requires a deviceId in either query or header', async () => {
    await expect(
      controller.getUsageByPurpose(
        {
          anonymousUserId: undefined,
          date: undefined,
          deviceId: undefined,
          from: undefined,
          period: '7d',
          timezone: undefined,
          to: undefined,
        },
        { userId: 'user-1' },
        undefined,
        undefined,
      ),
    ).rejects.toBeInstanceOf(BadRequestException);
  });
});
