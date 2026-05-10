import { HydratedDocument } from 'mongoose';
import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';

export type UsageSessionDocument = HydratedDocument<UsageSession>;

@Schema({ timestamps: true })
export class UsageSession {
  @Prop({ required: true, index: true })
  anonymousUserId: string;

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

UsageSessionSchema.index({ clientDeviceId: 1, deviceLocalDate: 1 });
UsageSessionSchema.index({ deviceId: 1, deviceLocalDate: 1 });
