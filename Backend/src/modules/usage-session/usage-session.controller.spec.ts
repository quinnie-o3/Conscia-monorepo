import { UsageSessionController } from './usage-session.controller';

describe('UsageSessionController', () => {
  const usageSessionService = {
    sync: jest.fn(),
  };

  let controller: UsageSessionController;

  beforeEach(() => {
    jest.clearAllMocks();
    controller = new UsageSessionController(usageSessionService as never);
  });

  it('wraps sync results in the standard API envelope', async () => {
    usageSessionService.sync.mockResolvedValue({
      insertedCount: 1,
      matchedCount: 0,
      processedCount: 1,
      updatedCount: 0,
    });

    await expect(
      controller.sync(
        { userId: 'user-1' },
        {
          sessions: [],
        },
      ),
    ).resolves.toEqual({
      success: true,
      message: 'Usage sessions synced successfully',
      data: {
        insertedCount: 1,
        matchedCount: 0,
        processedCount: 1,
        updatedCount: 0,
      },
    });
  });
});
