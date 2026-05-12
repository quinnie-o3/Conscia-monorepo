import { Injectable } from '@nestjs/common';

@Injectable()
export class PurposeTagService {
  findAll() {
    return [
      {
        id: 'learning',
        name: 'Learning',
        description: 'Dùng app cho học tập',
      },
      {
        id: 'work',
        name: 'Work',
        description: 'Dùng app cho công việc',
      },
      {
        id: 'entertainment',
        name: 'Entertainment',
        description: 'Giải trí',
      },
      {
        id: 'social',
        name: 'Social',
        description: 'Mạng xã hội/liên lạc',
      },
      {
        id: 'other',
        name: 'Other',
        description: 'Khác',
      },
    ];
  }
}