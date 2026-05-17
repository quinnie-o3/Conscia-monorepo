import {
  ConflictException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import * as bcrypt from 'bcrypt';
import { Model } from 'mongoose';

import { normalizeOptionalString } from '../../common/device-identity.util';
import { User, UserDocument } from './user.schema';
import { UpdateUserDto } from './dto/update-user.dto';

export type PublicUser = {
  avatarUrl?: string;
  displayName: string;
  email: string;
  googleId?: string;
  id: string;
  isOnboardingCompleted: boolean;
};

export type GoogleProfileData = {
  avatarUrl?: string;
  displayName?: string;
  email?: string;
  googleId: string;
};

type CreateLocalUserInput = {
  avatarUrl?: string;
  displayName?: string;
  email: string;
  password: string;
};

@Injectable()
export class UserService {
  private readonly saltRounds = 10;

  constructor(
    @InjectModel(User.name)
    private readonly userModel: Model<User>,
  ) {}

  private normalizeEmail(email: string) {
    return email.trim().toLowerCase();
  }

  private normalizePublicUser(user: UserDocument | User): PublicUser {
    const plainUser =
      'toObject' in user
        ? (user.toObject() as any)
        : ({ ...user } as any);

    const email = plainUser.email || '';
    const fallbackName = email.split('@')[0] || 'User';

    return {
      avatarUrl: plainUser.avatarUrl,
      displayName: plainUser.displayName || fallbackName,
      email: email,
      googleId: plainUser.googleId,
      id: (plainUser._id || plainUser.id).toString(),
      isOnboardingCompleted: plainUser.isOnboardingCompleted || false,
    };
  }

  async findById(id: string) {
    return this.userModel.findById(id).exec();
  }

  async findByEmail(email: string, includePassword = false) {
    const query = this.userModel.findOne({
      email: this.normalizeEmail(email),
    });

    if (includePassword) {
      query.select('+password');
    }

    return query.exec();
  }

  async findByGoogleId(googleId: string) {
    return this.userModel.findOne({ googleId }).exec();
  }

  async createLocalUser(input: CreateLocalUserInput) {
    const email = this.normalizeEmail(input.email);
    const existingUser = await this.userModel.exists({ email });

    if (existingUser) {
      throw new ConflictException('Email is already registered');
    }

    const password = await bcrypt.hash(input.password, this.saltRounds);
    const displayName = normalizeOptionalString(input.displayName) || email.split('@')[0];

    const user = await this.userModel.create({
      avatarUrl: normalizeOptionalString(input.avatarUrl),
      displayName: displayName,
      email,
      password,
      isOnboardingCompleted: false,
    });

    return user;
  }

  async comparePassword(user: UserDocument, password: string) {
    if (!user.password) {
      throw new UnauthorizedException('Invalid email or password');
    }

    return bcrypt.compare(password, user.password);
  }

  async updatePasswordByEmail(email: string, newPassword: string) {
    const normalizedEmail = this.normalizeEmail(email);
    const password = await bcrypt.hash(newPassword, this.saltRounds);
    return this.userModel
      .findOneAndUpdate(
        { email: normalizedEmail },
        { $set: { password } },
        { new: true },
      )
      .exec();
  }

  async createGoogleUser(profile: GoogleProfileData) {
    const email = profile.email ? this.normalizeEmail(profile.email) : undefined;
    const displayName = normalizeOptionalString(profile.displayName) || email?.split('@')[0] || 'Google User';

    return this.userModel.create({
      avatarUrl: normalizeOptionalString(profile.avatarUrl),
      displayName: displayName,
      email,
      googleId: profile.googleId,
      isOnboardingCompleted: false,
    });
  }

  async update(userId: string, dto: UpdateUserDto) {
    return this.userModel
      .findByIdAndUpdate(userId, { $set: dto }, { new: true })
      .exec();
  }

  async attachGoogleAccount(userId: string, profile: GoogleProfileData) {
    const update: any = { googleId: profile.googleId };

    const user = await this.userModel.findById(userId);
    if (user) {
        if (!user.displayName && profile.displayName) update.displayName = profile.displayName;
        if (!user.avatarUrl && profile.avatarUrl) update.avatarUrl = profile.avatarUrl;
    }

    return this.userModel
      .findByIdAndUpdate(userId, { $set: update }, { new: true })
      .exec();
  }

  toPublicUser(user: UserDocument | User): PublicUser | null {
    if (!user) return null;
    return this.normalizePublicUser(user);
  }
}
