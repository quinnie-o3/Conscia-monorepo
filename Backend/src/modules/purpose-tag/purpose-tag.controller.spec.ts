import { Test, TestingModule } from '@nestjs/testing';
import { PurposeTagController } from './purpose-tag.controller';
import { PurposeTagService } from './purpose-tag.service';

describe('PurposeTagController', () => {
  let controller: PurposeTagController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [PurposeTagController],
      providers: [
        {
          provide: PurposeTagService,
          useValue: {
            findAll: jest.fn(),
          },
        },
      ],
    }).compile();

    controller = module.get<PurposeTagController>(PurposeTagController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
