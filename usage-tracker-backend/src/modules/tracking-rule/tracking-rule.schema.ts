import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';

export type TrackingRuleDocument = HydratedDocument<TrackingRule>;

@Schema({ timestamps: true })
export class TrackingRule {
  @Prop({ required: true, index: true })
  anonymousUserId: string;

  @Prop({ required: true, index: true })
  deviceId: string;

  @Prop({ required: true, index: true })
  packageName: string;

  @Prop({ required: true })
  appName: string;

  @Prop()
  purposeTag?: string;

  @Prop()
  intentionLabel?: string;

  @Prop({ required: true, min: 1 })
  dailyLimitMinutes: number;

  @Prop({ default: true })
  trackingEnabled: boolean;

  @Prop({ default: true })
  warningEnabled: boolean;
}

export const TrackingRuleSchema =
  SchemaFactory.createForClass(TrackingRule);

TrackingRuleSchema.index(
  { anonymousUserId: 1, deviceId: 1, packageName: 1 },
  { unique: true },
);
