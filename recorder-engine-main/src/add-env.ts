/**
 * add-env.ts — Set URL for an environment (null → real URL) or add new env to an app.
 */

import * as fs from 'fs';
import * as path from 'path';
import * as yaml from 'js-yaml';
import { loadConfig, isSchemaV2 } from './config';

export interface AddEnvOptions {
  app: string;
  env: string;
  url: string;
  chainDir?: string;
}

export function addEnv(opts: AddEnvOptions): void {
  const chainDir = opts.chainDir || process.cwd();
  const configPath = path.join(chainDir, 'config', 'environments.yaml');

  if (!fs.existsSync(configPath)) {
    throw new Error(`Config not found: ${configPath}`);
  }

  if (!isSchemaV2(configPath)) {
    throw new Error(
      'Config dùng schema cũ. Migrate trước: frt-test migrate-config --write'
    );
  }

  // Validate env name
  if (!/^[a-z0-9-]+$/.test(opts.env)) {
    throw new Error(`Env name must be lowercase alphanumeric + dash: got "${opts.env}"`);
  }

  const raw = yaml.load(fs.readFileSync(configPath, 'utf8')) as any;

  if (!raw.apps[opts.app]) {
    const available = Object.keys(raw.apps).join(', ');
    throw new Error(`App "${opts.app}" not found. Available: ${available}`);
  }

  if (!raw.apps[opts.app].urls) {
    raw.apps[opts.app].urls = {};
  }

  // Check if env exists in top-level environments
  const isNewEnv = !raw.environments?.[opts.env];
  if (isNewEnv) {
    console.log(`⚠️  Env "${opts.env}" chưa có trong environments: block. Đang thêm...`);
    if (!raw.environments) raw.environments = {};
    raw.environments[opts.env] = { label: opts.env.toUpperCase() };
  }

  const oldUrl = raw.apps[opts.app].urls[opts.env];
  raw.apps[opts.app].urls[opts.env] = opts.url;

  // Write updated config
  fs.writeFileSync(configPath, yaml.dump(raw, { lineWidth: -1, noRefs: true, quotingType: '"' }));

  // Create env folder if not exists
  const envDir = path.join(chainDir, 'flows', opts.app, opts.env);
  if (!fs.existsSync(envDir)) {
    fs.mkdirSync(envDir, { recursive: true });
    fs.writeFileSync(path.join(envDir, '.gitkeep'), '');
  }

  const action = oldUrl == null ? 'Set' : 'Updated';
  console.log(`✅ ${action} URL cho ${opts.app} @ ${opts.env}: ${opts.url}`);
  if (oldUrl && oldUrl !== opts.url) {
    console.log(`   (trước đó: ${oldUrl})`);
  }
  console.log(`   Folder: flows/${opts.app}/${opts.env}/`);
}
