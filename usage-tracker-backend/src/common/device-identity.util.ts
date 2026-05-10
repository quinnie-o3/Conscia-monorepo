export function resolveAnonymousUserId(
  anonymousUserId: string | undefined,
  deviceId: string,
) {
  const normalizedAnonymousUserId = anonymousUserId?.trim();

  if (normalizedAnonymousUserId) {
    return normalizedAnonymousUserId;
  }

  return deviceId.trim();
}

export function normalizeOptionalString(value: string | undefined) {
  const normalizedValue = value?.trim();

  return normalizedValue ? normalizedValue : undefined;
}
