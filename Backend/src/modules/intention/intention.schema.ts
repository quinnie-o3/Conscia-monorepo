import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument, Schema as MongooseSchema } from 'mongoose';

export type IntentionDocument = HydratedDocument<Intention>;

@Schema({ timestamps: true })
export class Intention {
  @Prop({ type: MongooseSchema.Types.ObjectId, ref: 'User', index: true, default: null })
  userId: string | null; // null nếu là lý do hệ thống mặc định

  @Prop({ required: true })
  label: string;

  @Prop({ default: true })
  isSystem: boolean;
}

export const IntentionSchema = SchemaFactory.createForClass(Intention);
