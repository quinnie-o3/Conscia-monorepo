import { readFileSync, existsSync } from 'fs';
import { join } from 'path';
import mongoose from 'mongoose';

import {
  TrackingRule,
  TrackingRuleSchema,
} from '../modules/tracking-rule/tracking-rule.schema';

function loadEnvValue(name: string) {
  const envPath = join(process.cwd(), '.env');

  if (!existsSync(envPath)) {
    return process.env[name];
  }

  const line = readFileSync(envPath, 'utf8')
    .split(/\r?\n/)
    .find((entry) => entry.trim().startsWith(`${name}=`));

  if (!line) {
    return process.env[name];
  }

  return line.slice(line.indexOf('=') + 1).replace(/^['"]|['"]$/g, '');
}

async function main() {
  const uri = loadEnvValue('MONGODB_URI');

  if (!uri) {
    throw new Error('MONGODB_URI is not configured');
  }

  await mongoose.connect(uri, {
    connectTimeoutMS: 10000,
    serverSelectionTimeoutMS: 10000,
  });

  const TrackingRuleModel = mongoose.model(
    TrackingRule.name,
    TrackingRuleSchema,
  );
  const collection = TrackingRuleModel.collection;

  await collection.updateMany({ userId: null }, { $unset: { userId: '' } });

  const indexes = await collection.indexes();

  for (const index of indexes) {
    if (index.name && index.name !== '_id_') {
      await collection.dropIndex(index.name);
    }
  }

  await TrackingRuleModel.syncIndexes();

  console.log('Tracking rule indexes repaired successfully.');
}

main()
  .catch((error: Error) => {
    console.error(error.message);
    process.exitCode = 1;
  })
  .finally(async () => {
    await mongoose.disconnect();
  });
