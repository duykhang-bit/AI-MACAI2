/**
 * clone-flow.ts — Copy a YAML test flow between env folders.
 * Updates recorded_against metadata but does NOT replace URLs in steps.
 */

import * as fs from 'fs';
import * as path from 'path';
import * as yaml from 'js-yaml';
import { loadConfig } from './config';
import { FlowFile } from './types';

export interface CloneFlowOptions {
  app: string;
  from: string;
  to: string;
  file: string;
  chainDir?: string;
  overwrite?: boolean;
}

export function cloneFlow(opts: CloneFlowOptions): string {
  const chainDir = opts.chainDir || process.cwd();
  const configPath = path.join(chainDir, 'config', 'environments.yaml');
  const config = loadConfig(configPath);

  // Validate app
  if (!config.apps[opts.app]) {
    throw new Error(`App "${opts.app}" not found in config.`);
  }

  // Source file
  const srcDir = path.join(chainDir, 'flows', opts.app, opts.from);
  const srcFile = path.join(srcDir, opts.file);
  if (!fs.existsSync(srcFile)) {
    throw new Error(`Source file not found: ${path.relative(chainDir, srcFile)}`);
  }

  // Dest
  const dstDir = path.join(chainDir, 'flows', opts.app, opts.to);
  fs.mkdirSync(dstDir, { recursive: true });
  const dstFile = path.join(dstDir, opts.file);

  // Check collision
  if (fs.existsSync(dstFile) && !opts.overwrite) {
    throw new Error(
      `File already exists: ${path.relative(chainDir, dstFile)}. ` +
      `Use --overwrite to replace.`
    );
  }

  // Read and update metadata
  const content = fs.readFileSync(srcFile, 'utf8');
  const flow = yaml.load(content) as FlowFile;

  // Update recorded_against metadata
  const destUrl = config.apps[opts.app]?.urls?.[opts.to] || null;
  flow.recorded_against = {
    app: opts.app,
    env: opts.to,
    url: destUrl || '(not set)',
    date: new Date().toISOString().slice(0, 10),
  };

  // Write (preserving non-metadata content — use yaml dump)
  const newContent = yaml.dump(flow, { lineWidth: -1, noRefs: true, quotingType: '"' });
  fs.writeFileSync(dstFile, newContent);

  const relSrc = path.relative(chainDir, srcFile);
  const relDst = path.relative(chainDir, dstFile);
  console.log(`✅ Cloned: ${relSrc} → ${relDst}`);
  console.log(`   - URL trong steps: GIỮ NGUYÊN (không auto-replace).`);
  console.log(`   - recorded_against.env: ${opts.from} → ${opts.to}`);
  if (destUrl) {
    console.log(`   - recorded_against.url: ${destUrl}`);
  } else {
    console.log(`   ⚠️  URL env "${opts.to}" chưa set. Dùng: frt-test add-env --app ${opts.app} --env ${opts.to} --url <URL>`);
  }
  console.log(`   ⚠️  Verify lại trên ${opts.to} trước khi commit. DOM/flow có thể khác ${opts.from}.`);

  return dstFile;
}
