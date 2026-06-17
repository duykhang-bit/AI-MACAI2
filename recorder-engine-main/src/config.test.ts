/**
 * config.test.ts — Unit tests for config loader + resolver
 * Run: node --import tsx --test src/config.test.ts
 */

import { describe, it, beforeEach, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import { loadConfig, resolveBaseUrl, resolveEnv, listEnvironments, isSchemaV2 } from './config';

function writeTmpConfig(content: string): string {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'config-test-'));
  const cfgPath = path.join(dir, 'environments.yaml');
  fs.writeFileSync(cfgPath, content);
  return cfgPath;
}

// =============================================================================
// Schema v0 (legacy: environments key)
// =============================================================================
describe('Schema v0 (legacy)', () => {
  it('loads environments key as apps', () => {
    const cfgPath = writeTmpConfig(`
environments:
  rsa:
    baseUrl: "https://rsa.example.com"
  ecom:
    baseUrl: "https://ecom.example.com"
default: rsa
`);
    const config = loadConfig(cfgPath);
    assert.equal(config.apps.rsa.baseUrl, 'https://rsa.example.com');
    assert.equal(config.default_app, 'rsa');
  });

  it('resolveBaseUrl works with env="default"', () => {
    const cfgPath = writeTmpConfig(`
environments:
  rsa:
    baseUrl: "https://rsa.example.com"
default: rsa
`);
    const url = resolveBaseUrl('rsa', 'default', cfgPath);
    assert.equal(url, 'https://rsa.example.com');
  });

  it('resolveBaseUrl works without env', () => {
    const cfgPath = writeTmpConfig(`
environments:
  rsa:
    baseUrl: "https://rsa.example.com"
default: rsa
`);
    const url = resolveBaseUrl('rsa', undefined, cfgPath);
    assert.equal(url, 'https://rsa.example.com');
  });
});

// =============================================================================
// Schema v1 (current: apps with baseUrl string)
// =============================================================================
describe('Schema v1 (apps.baseUrl)', () => {
  it('loads apps with baseUrl', () => {
    const cfgPath = writeTmpConfig(`
apps:
  rsa:
    baseUrl: "https://ci-rsa.frt.vn"
    description: "RSA"
default_app: rsa
settings:
  timeout: 30000
`);
    const config = loadConfig(cfgPath);
    assert.equal(config.apps.rsa.baseUrl, 'https://ci-rsa.frt.vn');
    assert.equal(config.default_app, 'rsa');
  });

  it('resolveBaseUrl returns baseUrl for default env', () => {
    const cfgPath = writeTmpConfig(`
apps:
  rsa:
    baseUrl: "https://ci-rsa.frt.vn"
default_app: rsa
`);
    assert.equal(resolveBaseUrl('rsa', undefined, cfgPath), 'https://ci-rsa.frt.vn');
    assert.equal(resolveBaseUrl('rsa', 'default', cfgPath), 'https://ci-rsa.frt.vn');
  });

  it('resolveBaseUrl throws when --env is non-default on v1 schema', () => {
    const cfgPath = writeTmpConfig(`
apps:
  rsa:
    baseUrl: "https://ci-rsa.frt.vn"
default_app: rsa
`);
    assert.throws(
      () => resolveBaseUrl('rsa', 'ci', cfgPath),
      /schema cũ.*migrate/
    );
  });
});

// =============================================================================
// Schema v2 (multi-env: apps.X.urls)
// =============================================================================
describe('Schema v2 (multi-env)', () => {
  const v2Config = `
schema_version: 2
environments:
  ci:
    label: "CI"
  uat:
    label: "UAT"
  prod:
    label: "Production"
apps:
  rsa:
    description: "RSA Web"
    urls:
      ci: "https://ci-rsa.frt.vn"
      uat: null
      prod: null
  ecom:
    urls:
      ci: "https://ci-ecom.frt.vn"
      uat: "https://uat-ecom.frt.vn"
      prod: null
default_app: rsa
settings:
  timeout: 30000
`;

  it('detects schema v2', () => {
    const cfgPath = writeTmpConfig(v2Config);
    assert.equal(isSchemaV2(cfgPath), true);
  });

  it('loads schema v2 config correctly', () => {
    const cfgPath = writeTmpConfig(v2Config);
    const config = loadConfig(cfgPath);
    assert.equal(config.schema_version, 2);
    assert.deepEqual(config.environments?.ci, { label: 'CI' });
    assert.equal(config.apps.rsa.urls?.ci, 'https://ci-rsa.frt.vn');
    assert.equal(config.apps.rsa.urls?.uat, null);
  });

  it('resolveBaseUrl resolves env correctly', () => {
    const cfgPath = writeTmpConfig(v2Config);
    assert.equal(resolveBaseUrl('rsa', 'ci', cfgPath), 'https://ci-rsa.frt.vn');
    assert.equal(resolveBaseUrl('ecom', 'uat', cfgPath), 'https://uat-ecom.frt.vn');
  });

  it('resolveBaseUrl throws for null URL with helpful message', () => {
    const cfgPath = writeTmpConfig(v2Config);
    assert.throws(
      () => resolveBaseUrl('rsa', 'uat', cfgPath),
      /URL chưa được set.*add-env/
    );
  });

  it('resolveBaseUrl throws for unknown env with available list', () => {
    const cfgPath = writeTmpConfig(v2Config);
    assert.throws(
      () => resolveBaseUrl('rsa', 'staging', cfgPath),
      /không định nghĩa env "staging".*Có sẵn: ci, uat, prod/
    );
  });

  it('resolveBaseUrl throws for unknown app', () => {
    const cfgPath = writeTmpConfig(v2Config);
    assert.throws(
      () => resolveBaseUrl('nonexist', 'ci', cfgPath),
      /không có trong config.*Có sẵn: rsa, ecom/
    );
  });

  it('listEnvironments returns env names from environments block', () => {
    const cfgPath = writeTmpConfig(v2Config);
    const envs = listEnvironments(cfgPath);
    assert.deepEqual(envs, ['ci', 'uat', 'prod']);
  });
});

// =============================================================================
// Schema v2 detected by urls presence (without explicit schema_version)
// =============================================================================
describe('Schema v2 auto-detect (no schema_version)', () => {
  it('detects v2 when app has urls map', () => {
    const cfgPath = writeTmpConfig(`
apps:
  rsa:
    urls:
      ci: "https://ci-rsa.frt.vn"
      uat: null
default_app: rsa
`);
    assert.equal(isSchemaV2(cfgPath), true);
    assert.equal(resolveBaseUrl('rsa', 'ci', cfgPath), 'https://ci-rsa.frt.vn');
  });
});

// =============================================================================
// resolveEnv
// =============================================================================
describe('resolveEnv', () => {
  it('returns CLI flag when provided', () => {
    assert.equal(resolveEnv('uat'), 'uat');
  });

  it('falls back to FRT_TEST_ENV env var', () => {
    const orig = process.env.FRT_TEST_ENV;
    process.env.FRT_TEST_ENV = 'ci';
    try {
      assert.equal(resolveEnv(undefined), 'ci');
    } finally {
      if (orig === undefined) delete process.env.FRT_TEST_ENV;
      else process.env.FRT_TEST_ENV = orig;
    }
  });

  it('CLI flag takes priority over env var', () => {
    const orig = process.env.FRT_TEST_ENV;
    process.env.FRT_TEST_ENV = 'ci';
    try {
      assert.equal(resolveEnv('prod'), 'prod');
    } finally {
      if (orig === undefined) delete process.env.FRT_TEST_ENV;
      else process.env.FRT_TEST_ENV = orig;
    }
  });

  it('throws when neither flag nor env var set', () => {
    const orig = process.env.FRT_TEST_ENV;
    delete process.env.FRT_TEST_ENV;
    try {
      assert.throws(
        () => resolveEnv(undefined),
        /Thiếu --env.*FRT_TEST_ENV/
      );
    } finally {
      if (orig !== undefined) process.env.FRT_TEST_ENV = orig;
    }
  });
});

// =============================================================================
// Edge cases
// =============================================================================
describe('Edge cases', () => {
  it('loadConfig returns empty when file missing', () => {
    const config = loadConfig('/nonexistent/path/environments.yaml');
    assert.deepEqual(config.apps, {});
  });

  it('loadConfig handles empty file', () => {
    const cfgPath = writeTmpConfig('');
    const config = loadConfig(cfgPath);
    assert.deepEqual(config.apps, {});
  });

  it('resolveBaseUrl uses default_app when app not specified', () => {
    const cfgPath = writeTmpConfig(`
apps:
  rsa:
    baseUrl: "https://rsa.example.com"
default_app: rsa
`);
    assert.equal(resolveBaseUrl(undefined, undefined, cfgPath), 'https://rsa.example.com');
  });
});
