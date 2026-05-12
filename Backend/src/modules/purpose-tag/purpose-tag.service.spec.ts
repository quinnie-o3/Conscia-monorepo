import { Test, TestingModule } from '@nestjs/testing';
import { PurposeTagService } from './purpose-tag.service';

describe('PurposeTagService', () => {
  let service: PurposeTagService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [PurposeTagService],
    }).compile();

    service = module.get<PurposeTagService>(PurposeTagService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
