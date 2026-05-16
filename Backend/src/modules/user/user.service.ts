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
  displayName?: string;
  email?: string;
  googleId?: string;
  id: string;
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

  private normalizePublicUser(user: UserDocument | User) {
    const plainUser =
      'toObject' in user
        ? (user.toObject() as any)
        : ({ ...user } as any);

    return {
      avatarUrl: plainUser.avatarUrl,
      displayName: plainUser.displayName,
      email: plainUser.email,
      googleId: plainUser.googleId,
      id: (plainUser._id || plainUser.id).toString(),
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

    const user = await this.userModel.create({
      avatarUrl: normalizeOptionalString(input.avatarUrl),
      displayName:
        normalizeOptionalString(input.displayName) || email.split('@')[0],
      email,
      password,
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
    if (!profile.email) {
      throw new ConflictException(
        'Google account does not provide an email address',
      );
    }

    const email = this.normalizeEmail(profile.email);
    const existingUser = await this.userModel.exists({ email });

    if (existingUser) {
      // If email exists but no googleId, we might want to link them or throw conflict
      // For simplicity, let's link if it's the same email
      const user = await this.userModel.findOne({ email });
      if (user) {
         return this.attachGoogleAccount(user.id, profile);
      }
      throw new ConflictException('Email is already registered');
    }

    return this.userModel.create({
      avatarUrl: normalizeOptionalString(profile.avatarUrl),
      displayName:
        normalizeOptionalString(profile.displayName) || email.split('@')[0],
      email,
      googleId: profile.googleId,
    });
  }

  async update(userId: string, dto: UpdateUserDto) {
    return this.userModel
      .findByIdAndUpdate(userId, { $set: dto }, { new: true })
      .exec();
  }

  async attachGoogleAccount(
    userId: string,
    profile: GoogleProfileData,
  ) {
    const update: Record<string, unknown> = {
      googleId: profile.googleId,
    };
    const displayName = normalizeOptionalString(profile.displayName);
    const avatarUrl = normalizeOptionalString(profile.avatarUrl);

    if (displayName) {
      update.displayName = displayName;
    }

    if (avatarUrl) {
      update.avatarUrl = avatarUrl;
    }

    if (profile.email) {
      update.email = this.normalizeEmail(profile.email);
    }

    return this.userModel
      .findByIdAndUpdate(userId, { $set: update }, { new: true })
      .exec();
  }

  toPublicUser(user: UserDocument | User) {
    return this.normalizePublicUser(user);
  }
}
