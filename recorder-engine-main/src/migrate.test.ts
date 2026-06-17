/**
 * migrate.test.ts — Unit tests for v1→v2 config migration
 * Run: node --import tsx --test src/migrate.test.ts
 */

import { describe, it, before, after } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import { migrateConfig } from './migrate';

function createMockChain(dir: string): void {
  fs.mkdirSync(path.join(dir, 'config'), { recursive: true });
  fs.mkdirSync(path.join(dir, 'flows', 'rsa', '_shared'), { recursive: true });
  fs.mkdirSync(path.join(dir, 'flows', 'ecom'), { recursive: true });

  fs.writeFileSync(path.join(dir, 'config', 'environments.yaml'), `apps:
  rsa:
    baseUrl: "https://ci-rsa.frt.vn"
    description: "RSA"
  ecom:
    baseUrl: "https://ci-ecom.frt.vn"
    description: "Ecom"

default_app: rsa

settings:
  timeout: 30000
  headless: false
`);
  fs.writeFileSync(path.join(dir, 'flows', 'rsa', 'test-flow.yaml'), 'name: Test\nsteps: []\n');
  fs.writeFileSync(path.join(dir, 'flows', 'rsa', 'another.yaml'), 'name: Another\nsteps: []\n');
  fs.writeFileSync(path.join(dir, 'flows', 'rsa', '_shared', 'login.yaml'), 'name: Login\nsteps: []\n');
  fs.writeFileSync(path.join(dir, 'flows', 'ecom', 'checkout.yaml'), 'name: Checkout\nsteps: []\n');
}

describe('migrateConfig', () => {
  let tmpDir: string;

  before(() => {
    tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'migrate-test-'));
    createMockChain(tmpDir);
  });

  after(() => {
    fs.rmSync(tmpDir, { recursive: true, force: true });
  });

  it('dry-run does not modify files', () => {
    const result = migrateConfig({ chainDir: tmpDir, dryRun: true });
    // Config not changed
    const content = fs.readFileSync(path.join(tmpDir, 'config', 'environments.yaml'), 'utf8');
    assert.ok(content.includes('baseUrl:'));
    assert.ok(!content.includes('schema_version'));
    // Files not moved
    assert.ok(fs.existsSync(path.join(tmpDir, 'flows', 'rsa', 'test-flow.yaml')));
    // Result populated
    assert.ok(result.movedFiles.length > 0);
    assert.ok(result.configDiff.after.includes('schema_version'));
  });

  it('write mode converts config v1→v2', () => {
    const result = migrateConfig({ chainDir: tmpDir, write: true });

    const content = fs.readFileSync(path.join(tmpDir, 'config', 'environments.yaml'), 'utf8');
    assert.ok(content.includes('schema_version: 2'));
    assert.ok(content.includes('urls:'));
    assert.ok(content.includes('ci-rsa.frt.vn'));
    // uat and prod should be null
    assert.ok(content.includes('null'));
  });

  it('write mode creates backup', () => {
    assert.ok(fs.existsSync(path.join(tmpDir, 'config', 'environments.yaml.bak')));
  });

  it('write mode moves YAML files to ci/ subfolder', () => {
    // Moved
    assert.ok(fs.existsSync(path.join(tmpDir, 'flows', 'rsa', 'ci', 'test-flow.yaml')));
    assert.ok(fs.existsSync(path.join(tmpDir, 'flows', 'rsa', 'ci', 'another.yaml')));
    assert.ok(fs.existsSync(path.join(tmpDir, 'flows', 'ecom', 'ci', 'checkout.yaml')));
    // Original gone
    assert.ok(!fs.existsSync(path.join(tmpDir, 'flows', 'rsa', 'test-flow.yaml')));
  });

  it('preserves _shared folder', () => {
    assert.ok(fs.existsSync(path.join(tmpDir, 'flows', 'rsa', '_shared', 'login.yaml')));
  });

  it('creates uat/prod folders with .gitkeep', () => {
    assert.ok(fs.existsSync(path.join(tmpDir, 'flows', 'rsa', 'uat', '.gitkeep')));
    assert.ok(fs.existsSync(path.join(tmpDir, 'flows', 'rsa', 'prod', '.gitkeep')));
    assert.ok(fs.existsSync(path.join(tmpDir, 'flows', 'ecom', 'uat', '.gitkeep')));
  });

  it('does NOT modify YAML content', () => {
    const content = fs.readFileSync(path.join(tmpDir, 'flows', 'rsa', 'ci', 'test-flow.yaml'), 'utf8');
    assert.equal(content, 'name: Test\nsteps: []\n');
  });

  it('throws when already v2', () => {
    assert.throws(
      () => migrateConfig({ chainDir: tmpDir, write: true }),
      /đã là schema v2/
    );
  });
});
