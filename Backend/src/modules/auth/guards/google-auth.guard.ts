import { ExecutionContext, Injectable } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';

@Injectable()
export class GoogleAuthGuard extends AuthGuard('google') {
  getAuthenticateOptions(context: ExecutionContext) {
    const request = context.switchToHttp().getRequest();
    const tempDeviceId = request.query.tempDeviceId?.toString().trim();

    return {
      accessType: 'offline',
      prompt: 'select_account',
      scope: ['email', 'profile'],
      state: tempDeviceId || undefined,
    };
  }
}
