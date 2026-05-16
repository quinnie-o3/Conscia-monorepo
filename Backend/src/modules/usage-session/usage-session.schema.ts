import {
  HydratedDocument,
  Schema as MongooseSchema,
  Types,
} from 'mongoose';
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';

import { User } from '../user/user.schema';

export type UsageSessionDocument = HydratedDocument<UsageSession>;

@Schema({ timestamps: true })
export class UsageSession {
  @Prop({ index: true })
  anonymousUserId?: string;

  @Prop({
    index: true,
    ref: User.name,
    type: MongooseSchema.Types.ObjectId,
  })
  userId?: Types.ObjectId | string;

  @Prop({ required: true, index: true })
  deviceId: string;

  @Prop({ required: true, index: true })
  clientDeviceId: string;

  @Prop({ required: true, index: true })
  packageName: string;

  @Prop({ required: true })
  appName: string;

  @Prop()
  purposeTag?: string;

  @Prop()
  intentionLabel?: string;

  @Prop({ required: true })
  startedAt: Date;

  @Prop({ required: true })
  endedAt: Date;

  @Prop({ required: true, min: 0 })
  durationSeconds: number;

  @Prop({ required: true, index: true })
  deviceLocalDate: string;

  @Prop()
  deviceTimezone?: string;

  @Prop()
  timezoneOffsetMinutes?: number;

  @Prop({ default: false })
  trackingEnabled: boolean;

  @Prop({ default: false })
  warningEnabled: boolean;

  @Prop({ min: 0 })
  dailyLimitMinutes?: number;

  @Prop({ type: [String], default: [] })
  tags: string[];

  @Prop({ default: false })
  isClassified: boolean;

  @Prop({ required: true, unique: true, index: true })
  externalId: string;
}

export const UsageSessionSchema =
  SchemaFactory.createForClass(UsageSession);

UsageSessionSchema.index({
  anonymousUserId: 1,
  clientDeviceId: 1,
  deviceLocalDate: 1,
});
UsageSessionSchema.index({
  userId: 1,
  clientDeviceId: 1,
  deviceLocalDate: 1,
});
UsageSessionSchema.index({ clientDeviceId: 1, deviceLocalDate: 1 });
UsageSessionSchema.index({ deviceId: 1, deviceLocalDate: 1 });
