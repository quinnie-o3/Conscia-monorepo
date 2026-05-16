import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';

export type UserDocument = HydratedDocument<User>;

@Schema({ timestamps: true })
export class User {
  @Prop({
    index: true,
    lowercase: true,
    sparse: true,
    trim: true,
    unique: true,
  })
  email?: string;

  @Prop({ select: false })
  password?: string;

  @Prop({ index: true, sparse: true, unique: true })
  googleId?: string;

  @Prop({ trim: true })
  displayName?: string;

  @Prop({ trim: true })
  avatarUrl?: string;

  @Prop({ default: false })
  isOnboardingCompleted: boolean;
}

export const UserSchema = SchemaFactory.createForClass(User);
