# Backend / Frontend Alignment

## Base URL

- Local: `http://localhost:3000/api`
- Android emulator: `http://10.0.2.2:3000/api`
- Physical device: `http://<your-lan-ip>:3000/api`

## What the app really does today

- `App launch`: local only, no backend call.
- `Onboarding`: local only, stores `onboardingCompleted` and `deviceId`.
- `Dashboard`: local only, reads UsageStats + Room.
- `Rules`: local only, create/update/delete in Room.
- `Sessions / Intention`: local only, updates local `intentionLabel`.
- `Insights`: currently calculated on device.

Backend should therefore be treated as `sync + optional reporting`, not the main source of truth yet.

## Routes matched to the current app flow

### 1. Sync usage sessions

- Method: `POST /api/sessions/sync`
- Backward compatible alias: `POST /api/usage-sessions/sync`
- Purpose: receive batched Android usage sessions.

Request body:

```json
{
  "sessions": [
    {
      "externalId": "session-001",
      "deviceId": "device-demo-001",
      "packageName": "com.google.android.youtube",
      "appName": "YouTube",
      "startedAt": "2026-05-10T09:00:00.000Z",
      "endedAt": "2026-05-10T09:45:00.000Z",
      "durationSeconds": 2700,
      "deviceLocalDate": "2026-05-10",
      "deviceTimezone": "Asia/Saigon",
      "timezoneOffsetMinutes": 420,
      "intentionLabel": "Study video",
      "trackingEnabled": true,
      "warningEnabled": true,
      "dailyLimitMinutes": 60,
      "tags": ["Study video"],
      "isClassified": true
    }
  ]
}
```

Notes:

- Android currently sends `deviceId` per session item.
- `anonymousUserId` is optional.
- If `anonymousUserId` is missing, backend falls back to `deviceId`.
- Sync can be the first backend call. Device metadata is auto-created/upserted on sync.
- `deviceLocalDate` is the canonical day key used by the backend for daily and range stats.

Response:

```json
{
  "success": true,
  "message": "Usage sessions synced successfully",
  "data": {
    "processedCount": 1,
    "insertedCount": 1,
    "updatedCount": 0,
    "matchedCount": 0
  }
}
```

### 2. Usage by purpose

- Method: `GET /api/stats/usage-by-purpose`
- Purpose: aggregate synced sessions for Insights-like screens.

Query params:

- `deviceId`: required in query or header `x-device-id`
- `anonymousUserId`: optional
- `from` and `to`: optional explicit range in `yyyy-MM-dd`
- `period`: optional, supports `1d`, `7d`, `30d`, `day`, `week`, `month`
- `date`: optional anchor date in `yyyy-MM-dd`
- `timezone`: optional IANA timezone, also accepted from header `x-timezone`

Example:

```text
/api/stats/usage-by-purpose?deviceId=device-demo-001&from=2026-05-04&to=2026-05-10
```

Response:

```json
{
  "success": true,
  "message": "Usage by purpose retrieved successfully",
  "data": {
    "range": {
      "from": "2026-05-04",
      "to": "2026-05-10",
      "dayCount": 7
    },
    "summary": {
      "totalSeconds": 5400,
      "purposefulPercentage": 100,
      "distractingPercentage": 0,
      "trackedSeconds": 5400,
      "otherSeconds": 0,
      "trackedAppsCount": 1,
      "averageDailySeconds": 771
    },
    "details": [
      {
        "category": "TRACKED",
        "colorCode": "#006654",
        "tagName": "Learning",
        "duration": 5400,
        "percentage": 100
      }
    ],
    "topTrackedApp": {
      "packageName": "com.google.android.youtube",
      "appName": "YouTube",
      "totalDurationSeconds": 5400
    }
  }
}
```

How tracked usage is derived:

- session has `trackingEnabled = true`, or
- active backend rule exists for the same `packageName`, or
- session already carries `purposeTag` / `intentionLabel`

How stats stay aligned with the phone's local day:

- sync stores `deviceLocalDate` from Android and uses it directly for grouping
- when client sends only `period`, backend can use `timezone` or header `x-timezone` to resolve the anchor day

### 3. Daily stats

- Method: `GET /api/stats/daily`
- Purpose: day-level breakdown by app and limit warnings.

Query params:

- `deviceId`: required in query or header `x-device-id`
- `anonymousUserId`: optional
- `date`: required in `yyyy-MM-dd`

Example:

```text
/api/stats/daily?deviceId=device-demo-001&date=2026-05-10
```

Response:

```json
{
  "success": true,
  "message": "Daily summary retrieved successfully",
  "data": {
    "date": "2026-05-10",
    "totalDurationSeconds": 5400,
    "totalUsedMinutes": 90,
    "byPurpose": [
      {
        "purposeTag": "Study video",
        "durationSeconds": 5400,
        "usedMinutes": 90,
        "percentage": 100
      }
    ],
    "byApp": [
      {
        "packageName": "com.google.android.youtube",
        "appName": "YouTube",
        "purposeTag": "Study video",
        "durationSeconds": 5400,
        "usedMinutes": 90,
        "limitMinutes": 60,
        "isExceeded": true
      }
    ],
    "limitWarnings": [
      {
        "packageName": "com.google.android.youtube",
        "appName": "YouTube",
        "usedMinutes": 90,
        "limitMinutes": 60,
        "exceededByMinutes": 30
      }
    ]
  }
}
```

### 4. Optional device endpoints

- `POST /api/devices/register`
- `GET /api/devices`

Register body:

```json
{
  "deviceId": "device-demo-001",
  "anonymousUserId": "user-demo-001",
  "deviceName": "Pixel 7",
  "osVersion": "Android 14"
}
```

Notes:

- `anonymousUserId` is optional here as well.
- The current app flow does not need to call this endpoint before sync.

### 5. Optional tracking rule endpoints

- `POST /api/tracking-rules`
- `GET /api/tracking-rules?deviceId=...`
- `DELETE /api/tracking-rules?deviceId=...&packageName=...`

Example upsert body:

```json
{
  "deviceId": "device-demo-001",
  "anonymousUserId": "user-demo-001",
  "packageName": "com.google.android.youtube",
  "appName": "YouTube",
  "purposeTag": "Learning",
  "intentionLabel": "Study video",
  "dailyLimitMinutes": 60,
  "trackingEnabled": true,
  "warningEnabled": true
}
```

Notes:

- Rules are still optional from the backend point of view.
- If the app never syncs rules, backend can still store sessions, but stats will be less faithful to the local Insights screen.

### 6. Purpose tags

- Method: `GET /api/purpose-tags`
- Purpose: static tag list for clients that want starter labels from backend.

## What is still missing on the frontend side

These are not backend bugs. They are integration gaps if you want backend to become part of the real flow:

1. There is still no confirmed place in the app that actually calls `POST /api/sessions/sync`.
2. Insights currently runs fully local, so `GET /api/stats/usage-by-purpose` is prepared but not yet proven in the real UI flow.
3. Rules are not synced from Room to backend, so backend cannot always know which packages are considered tracked.
4. `intentionLabel` is still local-first. Backend can store it now, but the app still decides when to sync it.
5. If the app calls stats endpoints, it must send at least `deviceId` in query or `x-device-id` header.

## Recommended minimum integration order

1. Wire `POST /api/sessions/sync`.
2. Send `deviceId` on every stats request.
3. Replace local Insights data source with `GET /api/stats/usage-by-purpose` only after sync is stable.
4. Sync rules and `intentionLabel` if you want backend Insights to match local calculations more closely.
