/**
 * scaffold.test.ts — Unit tests for scaffold logic
 * Run: node --import tsx --test src/scaffold.test.ts
 */

import { describe, it, before, after } from 'node:test';
import assert from 'node:assert/strict';
import * as fs from 'fs';
import * as path from 'path';
import * as os from 'os';
import { renderTemplate, copyTemplateDir, scaffoldChain, addAppToChain, ScaffoldOpts } from './scaffold';

describe('renderTemplate', () => {
  it('replaces {{VAR}} placeholders', () => {
    const result = renderTemplate('Hello {{NAME}}, app: {{APP}}', { NAME: 'LAB', APP: 'rsa' });
    assert.equal(result, 'Hello LAB, app: rsa');
  });

  it('leaves unknown vars unchanged', () => {
    const result = renderTemplate('{{KNOWN}} {{UNKNOWN}}', { KNOWN: 'yes' });
    assert.equal(result, 'yes {{UNKNOWN}}');
  });

  it('handles empty string', () => {
    assert.equal(renderTemplate('', {}), '');
  });
});

describe('copyTemplateDir', () => {
  let tmpDir: string;

  before(() => {
    tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'scaffold-test-'));
    // Create a mini template
    const tplDir = path.join(tmpDir, 'tpl');
    fs.mkdirSync(path.join(tplDir, 'sub'), { recursive: true });
    fs.writeFileSync(path.join(tplDir, 'file.txt.tpl'), 'chain: {{NAME}}');
    fs.writeFileSync(path.join(tplDir, 'static.yaml'), 'no-change');
    fs.writeFileSync(path.join(tplDir, 'sub', 'nested.md.tpl'), '# {{TITLE}}');
  });

  after(() => {
    fs.rmSync(tmpDir, { recursive: true, force: true });
  });

  it('copies and renders .tpl files, keeps others unchanged', async () => {
    const outDir = path.join(tmpDir, 'out');
    await copyTemplateDir(path.join(tmpDir, 'tpl'), outDir, { NAME: 'LAB', TITLE: 'Hello' });

    assert.equal(fs.readFileSync(path.join(outDir, 'file.txt'), 'utf8'), 'chain: LAB');
    assert.equal(fs.readFileSync(path.join(outDir, 'static.yaml'), 'utf8'), 'no-change');
    assert.equal(fs.readFileSync(path.join(outDir, 'sub', 'nested.md'), 'utf8'), '# Hello');
    // .tpl extension removed
    assert.equal(fs.existsSync(path.join(outDir, 'file.txt.tpl')), false);
  });
});

describe('scaffoldChain', () => {
  let tmpDir: string;

  before(() => {
    tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'scaffold-chain-'));
  });

  after(() => {
    fs.rmSync(tmpDir, { recursive: true, force: true });
  });

  it('rejects non-kebab-case name', async () => {
    await assert.rejects(
      () => scaffoldChain({ name: 'NotKebab', display: 'X', apps: [{ name: 'a', url: 'http://x' }], envs: ['ci', 'uat', 'prod'], output: path.join(tmpDir, 'x') }),
      /kebab-case/,
    );
  });

  it('rejects empty apps', async () => {
    await assert.rejects(
      () => scaffoldChain({ name: 'test', display: 'X', apps: [], envs: ['ci', 'uat', 'prod'], output: path.join(tmpDir, 'y') }),
      /At least 1 app/,
    );
  });

  it('rejects non-empty output dir', async () => {
    const existDir = path.join(tmpDir, 'existing');
    fs.mkdirSync(existDir, { recursive: true });
    fs.writeFileSync(path.join(existDir, 'file.txt'), 'x');
    await assert.rejects(
      () => scaffoldChain({ name: 'test', display: 'X', apps: [{ name: 'a', url: 'http://x' }], envs: ['ci', 'uat', 'prod'], output: existDir }),
      /not empty/,
    );
  });

  it('creates valid chain skeleton', async () => {
    const outDir = path.join(tmpDir, 'my-chain');
    await scaffoldChain({
      name: 'my-chain',
      display: 'My Chain',
      apps: [
        { name: 'app-one', url: 'https://one.example.com', urls: { ci: 'https://one.example.com', uat: null, prod: null } },
        { name: 'app-two', url: 'https://two.example.com', urls: { ci: 'https://two.example.com', uat: null, prod: null } },
      ],
      envs: ['ci', 'uat', 'prod'],
      output: outDir,
    });

    // Config rendered
    const config = fs.readFileSync(path.join(outDir, 'config', 'environments.yaml'), 'utf8');
    assert.ok(config.includes('app-one:'));
    assert.ok(config.includes('https://one.example.com'));
    assert.ok(config.includes('default_app: app-one'));

    // README rendered
    const readme = fs.readFileSync(path.join(outDir, 'README.md'), 'utf8');
    assert.ok(readme.includes('My Chain'));
    assert.ok(!readme.includes('{{'));

    // Flow folders
    assert.ok(fs.existsSync(path.join(outDir, 'flows', 'app-one', '_shared')));
    assert.ok(fs.existsSync(path.join(outDir, 'flows', 'app-two', '_shared')));
    // Env folders with .gitkeep
    assert.ok(fs.existsSync(path.join(outDir, 'flows', 'app-one', 'ci', '.gitkeep')));
    assert.ok(fs.existsSync(path.join(outDir, 'flows', 'app-one', 'uat', '.gitkeep')));
    assert.ok(fs.existsSync(path.join(outDir, 'flows', 'app-one', 'prod', '.gitkeep')));

    // Git initialized
    assert.ok(fs.existsSync(path.join(outDir, '.git')));
  });
});

describe('addAppToChain', () => {
  let tmpDir: string;

  before(async () => {
    tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'addapp-'));
    // Create minimal chain structure
    fs.mkdirSync(path.join(tmpDir, 'config'), { recursive: true });
    fs.mkdirSync(path.join(tmpDir, 'flows'), { recursive: true });
    fs.writeFileSync(path.join(tmpDir, 'config', 'environments.yaml'), `apps:
  existing-app:
    baseUrl: "https://existing.com"
    description: "Existing"

default_app: existing-app

settings:
  timeout: 30000
`);
  });

  after(() => {
    fs.rmSync(tmpDir, { recursive: true, force: true });
  });

  it('adds app to config + creates flow folder', async () => {
    await addAppToChain({ name: 'new-app', url: 'https://new.example.com', chainDir: tmpDir });

    const config = fs.readFileSync(path.join(tmpDir, 'config', 'environments.yaml'), 'utf8');
    assert.ok(config.includes('new-app:'));
    assert.ok(config.includes('https://new.example.com'));
    assert.ok(fs.existsSync(path.join(tmpDir, 'flows', 'new-app', '_shared')));
  });

  it('rejects duplicate app', async () => {
    await assert.rejects(
      () => addAppToChain({ name: 'existing-app', url: 'http://x', chainDir: tmpDir }),
      /already exists/,
    );
  });
});
