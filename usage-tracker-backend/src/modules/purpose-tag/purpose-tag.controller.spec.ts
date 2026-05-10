import { Test, TestingModule } from '@nestjs/testing';
import { PurposeTagController } from './purpose-tag.controller';

describe('PurposeTagController', () => {
  let controller: PurposeTagController;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [PurposeTagController],
    }).compile();

    controller = module.get<PurposeTagController>(PurposeTagController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
