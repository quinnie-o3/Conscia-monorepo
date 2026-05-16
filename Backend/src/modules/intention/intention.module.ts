import { Module, OnModuleInit } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { Intention, IntentionSchema } from './intention.schema';
import { IntentionService } from './intention.service';
import { IntentionController } from './intention.controller';

@Module({
  imports: [
    MongooseModule.forFeature([{ name: Intention.name, schema: IntentionSchema }]),
  ],
  controllers: [IntentionController],
  providers: [IntentionService],
  exports: [IntentionService],
})
export class IntentionModule implements OnModuleInit {
  constructor(private readonly intentionService: IntentionService) {}

  async onModuleInit() {
    await this.intentionService.seedDefaultIntentions();
  }
}
