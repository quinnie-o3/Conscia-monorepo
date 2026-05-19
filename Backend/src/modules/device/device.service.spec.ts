import { Test, TestingModule } from '@nestjs/testing';
import { getModelToken } from '@nestjs/mongoose';
import { DeviceService } from './device.service';
import { Device } from './device.schema';

describe('DeviceService', () => {
  let service: DeviceService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        DeviceService,
        {
          provide: getModelToken(Device.name),
          useValue: {},
        },
      ],
    }).compile();

    service = module.get<DeviceService>(DeviceService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
