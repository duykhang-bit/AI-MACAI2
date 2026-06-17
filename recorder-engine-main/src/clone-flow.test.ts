/**
 * clone-flow.test.ts — Unit tests for clone-flow command
 * Run: node --import tsx --test src/clone-flow.test.ts
 */

import { describe, it, before, after } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import * as yaml from 'js-yaml';
import { cloneFlow } from './clone-flow';

function createMockV2Chain(dir: string): void {
  fs.mkdirSync(path.join(dir, 'config'), { recursive: true });
  fs.mkdirSync(path.join(dir, 'flows', 'rsa', 'ci'), { recursive: true });
  fs.mkdirSync(path.join(dir, 'flows', 'rsa', 'uat'), { recursive: true });

  fs.writeFileSync(path.join(dir, 'config', 'environments.yaml'), `schema_version: 2
environments:
  ci:
    label: "CI"
  uat:
    label: "UAT"
  prod:
    label: "Production"
apps:
  rsa:
    description: "RSA"
    urls:
      ci: "https://ci-rsa.frt.vn"
      uat: "https://uat-rsa.frt.vn"
      prod: null
default_app: rsa
`);

  const flowYaml = yaml.dump({
    name: 'Test Flow',
    recorded_against: { app: 'rsa', env: 'ci', url: 'https://ci-rsa.frt.vn', date: '2026-06-01' },
    config: { baseUrl: 'https://ci-rsa.frt.vn' },
    steps: [
      { name: 'Go to home', action: 'goto', value: 'https://ci-rsa.frt.vn' },
      { name: 'Click button', action: 'click', selector: '#btn' },
    ],
  });
  fs.writeFileSync(path.join(dir, 'flows', 'rsa', 'ci', 'test-flow.yaml'), flowYaml);
}

describe('cloneFlow', () => {
  let tmpDir: string;

  before(() => {
    tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'clone-flow-test-'));
    createMockV2Chain(tmpDir);
  });

  after(() => {
    fs.rmSync(tmpDir, { recursive: true, force: true });
  });

  it('copies file to destination env folder', () => {
    const dest = cloneFlow({
      app: 'rsa', from: 'ci', to: 'uat', file: 'test-flow.yaml', chainDir: tmpDir,
    });
    assert.ok(fs.existsSync(dest));
    assert.ok(dest.includes(path.join('rsa', 'uat', 'test-flow.yaml')));
  });

  it('updates recorded_against metadata', () => {
    const content = fs.readFileSync(
      path.join(tmpDir, 'flows', 'rsa', 'uat', 'test-flow.yaml'), 'utf8'
    );
    const flow = yaml.load(content) as any;
    assert.equal(flow.recorded_against.env, 'uat');
    assert.equal(flow.recorded_against.url, 'https://uat-rsa.frt.vn');
    assert.equal(flow.recorded_against.app, 'rsa');
  });

  it('preserves URL in steps (does NOT replace)', () => {
    const content = fs.readFileSync(
      path.join(tmpDir, 'flows', 'rsa', 'uat', 'test-flow.yaml'), 'utf8'
    );
    const flow = yaml.load(content) as any;
    assert.equal(flow.steps[0].value, 'https://ci-rsa.frt.vn');
    assert.equal(flow.config.baseUrl, 'https://ci-rsa.frt.vn');
  });

  it('throws on collision without --overwrite', () => {
    assert.throws(
      () => cloneFlow({
        app: 'rsa', from: 'ci', to: 'uat', file: 'test-flow.yaml', chainDir: tmpDir,
      }),
      /already exists/
    );
  });

  it('overwrites with --overwrite flag', () => {
    cloneFlow({
      app: 'rsa', from: 'ci', to: 'uat', file: 'test-flow.yaml',
      chainDir: tmpDir, overwrite: true,
    });
    assert.ok(fs.existsSync(path.join(tmpDir, 'flows', 'rsa', 'uat', 'test-flow.yaml')));
  });

  it('creates dest folder if not exists', () => {
    // Clone to prod (folder exists but empty)
    fs.mkdirSync(path.join(tmpDir, 'flows', 'rsa', 'prod'), { recursive: true });
    const dest = cloneFlow({
      app: 'rsa', from: 'ci', to: 'prod', file: 'test-flow.yaml', chainDir: tmpDir,
    });
    assert.ok(fs.existsSync(dest));
    // recorded_against.url should be "(not set)" since prod URL is null
    const flow = yaml.load(fs.readFileSync(dest, 'utf8')) as any;
    assert.equal(flow.recorded_against.url, '(not set)');
  });

  it('throws for non-existent source file', () => {
    assert.throws(
      () => cloneFlow({
        app: 'rsa', from: 'ci', to: 'uat', file: 'nonexist.yaml', chainDir: tmpDir,
      }),
      /Source file not found/
    );
  });
});
