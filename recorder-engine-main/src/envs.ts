/**
 * envs.ts — List environments and their URL status per app.
 */

import * as path from 'path';
import { loadConfig, isSchemaV2, listEnvironments } from './config';

export interface ListEnvsOptions {
  chainDir?: string;
}

export function listEnvs(opts: ListEnvsOptions = {}): void {
  const chainDir = opts.chainDir || process.cwd();
  const configPath = path.join(chainDir, 'config', 'environments.yaml');
  const config = loadConfig(configPath);

  if (!isSchemaV2(configPath)) {
    // v1: just show apps with their single baseUrl
    console.log('\n📋 Config schema v1 (single URL per app):\n');
    for (const [appName, appCfg] of Object.entries(config.apps)) {
      console.log(`  ${appName}: ${appCfg.baseUrl || '(no URL)'}`);
    }
    console.log('\n💡 Migrate sang multi-env: frt-test migrate-config\n');
    return;
  }

  const envs = listEnvironments(configPath);
  console.log('\n📋 Environments:\n');

  // Header
  const envHeader = envs.map(e => e.padEnd(12)).join('');
  console.log(`  ${'App'.padEnd(20)} ${envHeader}`);
  console.log(`  ${'─'.repeat(20)} ${envs.map(() => '─'.repeat(12)).join('')}`);

  // Rows
  for (const [appName, appCfg] of Object.entries(config.apps)) {
    const cols = envs.map(env => {
      const url = appCfg.urls?.[env];
      if (url == null) return '⚠ not set'.padEnd(12);
      return ('✓ ' + truncUrl(url)).padEnd(12);
    }).join('');
    console.log(`  ${appName.padEnd(20)} ${cols}`);
  }

  console.log('');

  // Detail per app
  for (const [appName, appCfg] of Object.entries(config.apps)) {
    if (!appCfg.urls) continue;
    const nullEnvs = Object.entries(appCfg.urls)
      .filter(([, url]) => url == null)
      .map(([env]) => env);
    if (nullEnvs.length > 0) {
      console.log(`  💡 ${appName}: set URL cho ${nullEnvs.join(', ')} bằng:`);
      for (const env of nullEnvs) {
        console.log(`     frt-test add-env --app ${appName} --env ${env} --url <URL>`);
      }
    }
  }
  console.log('');
}

function truncUrl(url: string): string {
  if (url.length <= 30) return url;
  try {
    const u = new URL(url);
    return u.hostname.slice(0, 28);
  } catch {
    return url.slice(0, 28) + '…';
  }
}
