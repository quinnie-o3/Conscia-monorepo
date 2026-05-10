import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';

export type DeviceDocument = HydratedDocument<Device>;

@Schema({ timestamps: true })
export class Device {
  @Prop({ required: true, index: true })
  anonymousUserId: string;

  @Prop({ required: true, index: true })
  deviceId: string;

  @Prop()
  deviceName?: string;

  @Prop()
  osVersion?: string;

  @Prop({ default: 'android' })
  platform: string;

  @Prop({ default: true })
  isActive: boolean;

  @Prop()
  lastSyncAt?: Date;
}

export const DeviceSchema = SchemaFactory.createForClass(Device);

DeviceSchema.index(
  { anonymousUserId: 1, deviceId: 1 },
  { unique: true },
);