import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Intention, IntentionDocument } from './intention.schema';

@Injectable()
export class IntentionService {
  constructor(
    @InjectModel(Intention.name)
    private readonly intentionModel: Model<IntentionDocument>,
  ) {}

  async findAll(userId: string | null) {
    // Lấy lý do hệ thống (userId: null) và lý do của riêng User này
    return this.intentionModel
      .find({
        $or: [{ userId: null }, { userId }],
      })
      .sort({ isSystem: -1, createdAt: -1 })
      .exec();
  }

  async create(userId: string, label: string) {
    // Kiểm tra xem label đã tồn tại chưa
    const existing = await this.intentionModel.findOne({ userId, label }).exec();
    if (existing) return existing;

    const newIntention = new this.intentionModel({
      userId,
      label,
      isSystem: false,
    });
    return newIntention.save();
  }

  async delete(userId: string, id: string) {
    return this.intentionModel.findOneAndDelete({ _id: id, userId }).exec();
  }

  async seedDefaultIntentions() {
    const defaults = [
      'Stay focused on work',
      'Stay focused on study',
      'Avoid mindless scrolling',
      'Check updates without getting stuck',
      'Use it intentionally to relax',
      'Limit late-night usage',
    ];

    for (const label of defaults) {
      const count = await this.intentionModel.countDocuments({ label, isSystem: true });
      if (count === 0) {
        await new this.intentionModel({ label, isSystem: true, userId: null }).save();
      }
    }
  }
}
