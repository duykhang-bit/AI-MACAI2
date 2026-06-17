/**
 * Config loader — reads environments.yaml, supports schema v0/v1/v2.
 * v0 (legacy): { environments: { name: { baseUrl } }, default: string }
 * v1 (current): { apps: { name: { baseUrl } }, default_app: string, settings: {} }
 * v2 (multi-env): { schema_version: 2, environments: {...}, apps: { name: { urls: {env: url} } }, ... }
 */

import * as fs from 'fs';
import * as path from 'path';
import * as yaml from 'js-yaml';
import { EnvironmentMeta } from './types';

export interface AppConfig {
  baseUrl?: string;
  urls?: Record<string, string | null>;
  description?: string;
}

export interface EnvConfig {
  schema_version?: number;
  environments?: Record<string, EnvironmentMeta>;
  apps: Record<string, AppConfig>;
  default_app: string;
  settings: { timeout?: number; headless?: boolean; screenshotOnFail?: boolean };
}

const CONFIG_PATH = path.join(process.cwd(), 'config/environments.yaml');

export function loadConfig(configPath?: string): EnvConfig {
  const cfgPath = configPath || CONFIG_PATH;
  if (!fs.existsSync(cfgPath)) {
    return { apps: {}, default_app: '', settings: {} };
  }

  const raw = yaml.load(fs.readFileSync(cfgPath, 'utf8')) as any;
  if (!raw) return { apps: {}, default_app: '', settings: {} };

  // Schema v2: has apps with urls map (or explicit schema_version)
  if (raw.apps && (raw.schema_version === 2 || Object.values(raw.apps).some((a: any) => a?.urls))) {
    return {
      schema_version: 2,
      environments: raw.environments || {},
      apps: raw.apps,
      default_app: raw.default_app || Object.keys(raw.apps)[0] || '',
      settings: raw.settings || {},
    };
  }

  // Schema v1: apps with baseUrl string
  if (raw.apps) {
    return {
      apps: raw.apps,
      default_app: raw.default_app || Object.keys(raw.apps)[0] || '',
      settings: raw.settings || {},
    };
  }

  // Schema v0: environments key (legacy)
  if (raw.environments) {
    const apps: Record<string, AppConfig> = {};
    for (const [name, val] of Object.entries(raw.environments)) {
      apps[name] = val as AppConfig;
    }
    return {
      apps,
      default_app: raw.default || Object.keys(apps)[0] || '',
      settings: {},
    };
  }

  return { apps: {}, default_app: '', settings: {} };
}

export function resolveBaseUrl(app?: string, env?: string, configPath?: string): string {
  const config = loadConfig(configPath);
  const appName = app || config.default_app;
  if (!appName) {
    throw new Error('Không xác định được app. Truyền --app <name> hoặc set default_app trong config.');
  }

  const appCfg = config.apps[appName];
  if (!appCfg) {
    const available = Object.keys(config.apps).join(', ');
    throw new Error(`App "${appName}" không có trong config. Có sẵn: ${available}`);
  }

  // Schema v2 (urls map)
  if (appCfg.urls) {
    const envName = env || 'default';
    if (!(envName in appCfg.urls)) {
      const available = Object.keys(appCfg.urls).join(', ');
      throw new Error(
        `App "${appName}" không định nghĩa env "${envName}". Có sẵn: ${available}`
      );
    }
    const url = appCfg.urls[envName];
    if (url == null || url === '') {
      throw new Error(
        `App "${appName}" / env "${envName}": URL chưa được set (giá trị null). ` +
        `Sửa config/environments.yaml hoặc dùng: frt-test add-env --app ${appName} --env ${envName} --url <URL>`
      );
    }
    return url;
  }

  // Schema v1/v0 (legacy: single baseUrl string)
  if (appCfg.baseUrl) {
    if (env && env !== 'default') {
      throw new Error(
        `Chuỗi này dùng schema cũ (chưa migrate). --env "${env}" không support. ` +
        `Migrate bằng: frt-test migrate-config`
      );
    }
    return appCfg.baseUrl;
  }

  throw new Error(`App "${appName}" không có baseUrl hoặc urls trong config.`);
}

export function resolveEnv(envOpt?: string): string {
  const env = envOpt || process.env.FRT_TEST_ENV;
  if (!env) {
    throw new Error(
      'Thiếu --env. Phải chỉ định môi trường: --env ci|uat|prod ' +
      '(hoặc set biến môi trường FRT_TEST_ENV)'
    );
  }
  return env;
}

export function listEnvironments(configPath?: string): string[] {
  const config = loadConfig(configPath);
  if (config.environments) {
    return Object.keys(config.environments);
  }
  // Fallback: collect env keys from all apps' urls maps
  const envSet = new Set<string>();
  for (const appCfg of Object.values(config.apps)) {
    if (appCfg.urls) {
      for (const env of Object.keys(appCfg.urls)) {
        envSet.add(env);
      }
    }
  }
  return [...envSet];
}

export function isSchemaV2(configPath?: string): boolean {
  const config = loadConfig(configPath);
  return config.schema_version === 2;
}
