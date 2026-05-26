import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument, Schema as MongooseSchema, Types } from 'mongoose';
import { User } from '../user/user.schema';

export type TrackingRuleDocument = HydratedDocument<TrackingRule>;

@Schema({ timestamps: true })
export class TrackingRule {
  @Prop({ index: true })
  anonymousUserId?: string;

  @Prop({ type: MongooseSchema.Types.ObjectId, ref: User.name, index: true })
  userId?: Types.ObjectId | string;

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

  @Prop({ default: 0 })
  extensionMinutes: number;

  @Prop({ default: 0 })
  extensionCount: number;

  @Prop()
  lastExtensionDate?: string;
}

export const TrackingRuleSchema = SchemaFactory.createForClass(TrackingRule);

TrackingRuleSchema.index(
  { userId: 1, packageName: 1 },
  {
    partialFilterExpression: {
      userId: { $exists: true, $type: 'objectId' },
    },
    unique: true,
  },
);
TrackingRuleSchema.index({ anonymousUserId: 1, deviceId: 1 });
