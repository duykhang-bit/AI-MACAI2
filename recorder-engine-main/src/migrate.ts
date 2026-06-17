/**
 * migrate.ts — Migrate chain config from schema v1 → v2 (multi-env)
 * - Converts apps.X.baseUrl → apps.X.urls.ci (uat/prod = null)
 * - Moves flows/{app}/*.yaml → flows/{app}/ci/*.yaml via git mv
 * - Preserves _shared/ folders
 * - Creates uat/prod/.gitkeep placeholders
 */

import * as fs from 'fs';
import * as path from 'path';
import * as yaml from 'js-yaml';
import { execFileSync } from 'child_process';
import { loadConfig, isSchemaV2 } from './config';

export interface MigrateOptions {
  dryRun?: boolean;
  write?: boolean;
  chainDir?: string;
  envs?: string[];
}

export interface MigrateResult {
  configDiff: { before: string; after: string };
  movedFiles: Array<{ from: string; to: string }>;
  createdDirs: string[];
  backupPath?: string;
}

function isGitRepo(dir: string): boolean {
  return fs.existsSync(path.join(dir, '.git'));
}

function moveFile(from: string, to: string, useGit: boolean, cwd: string): void {
  fs.mkdirSync(path.dirname(to), { recursive: true });
  if (useGit) {
    try {
      execFileSync('git', ['mv', path.relative(cwd, from), path.relative(cwd, to)], { cwd });
      return;
    } catch {
      // fallback to fs rename if git mv fails (file untracked)
    }
  }
  fs.renameSync(from, to);
}

export function migrateConfig(opts: MigrateOptions = {}): MigrateResult {
  const chainDir = opts.chainDir || process.cwd();
  const configPath = path.join(chainDir, 'config', 'environments.yaml');
  const envs = opts.envs || ['ci', 'uat', 'prod'];

  if (!fs.existsSync(configPath)) {
    throw new Error(`Config not found: ${configPath}`);
  }

  if (isSchemaV2(configPath)) {
    throw new Error('Config đã là schema v2. Không cần migrate.');
  }

  const config = loadConfig(configPath);
  const rawContent = fs.readFileSync(configPath, 'utf8');

  // Build new v2 config
  const v2: any = {
    schema_version: 2,
    environments: {} as any,
    apps: {} as any,
    default_app: config.default_app,
    settings: config.settings,
  };

  for (const env of envs) {
    v2.environments[env] = { label: env.toUpperCase() };
  }

  for (const [appName, appCfg] of Object.entries(config.apps)) {
    v2.apps[appName] = {
      description: appCfg.description || appName,
      urls: {} as any,
    };
    for (const env of envs) {
      // Current URL goes to first env (ci), rest = null
      v2.apps[appName].urls[env] = env === envs[0] ? (appCfg.baseUrl || null) : null;
    }
  }

  const newContent = yaml.dump(v2, { lineWidth: -1, noRefs: true, quotingType: '"' });

  // Collect YAML files to move
  const flowsDir = path.join(chainDir, 'flows');
  const movedFiles: Array<{ from: string; to: string }> = [];
  const createdDirs: string[] = [];
  const useGit = isGitRepo(chainDir);
  const targetEnv = envs[0]; // move existing files to first env (ci)

  if (fs.existsSync(flowsDir)) {
    for (const appName of Object.keys(config.apps)) {
      const appDir = path.join(flowsDir, appName);
      if (!fs.existsSync(appDir)) continue;

      for (const entry of fs.readdirSync(appDir, { withFileTypes: true })) {
        // Skip _shared and existing env folders
        if (entry.isDirectory()) continue;
        if (!entry.name.endsWith('.yaml') && !entry.name.endsWith('.yml')) continue;

        const from = path.join(appDir, entry.name);
        const to = path.join(appDir, targetEnv, entry.name);
        movedFiles.push({ from, to });
      }

      // Create env folders
      for (const env of envs) {
        const envDir = path.join(appDir, env);
        if (!fs.existsSync(envDir)) {
          createdDirs.push(envDir);
        }
      }
    }
  }

  const result: MigrateResult = {
    configDiff: { before: rawContent, after: newContent },
    movedFiles,
    createdDirs,
  };

  // Dry run — print what would happen
  if (!opts.write) {
    console.log('🔍 DRY RUN — no changes will be made.\n');
    console.log('📄 Config diff:');
    console.log('  BEFORE (schema v1):');
    for (const line of rawContent.split('\n').slice(0, 10)) console.log(`    ${line}`);
    console.log('  ...\n  AFTER (schema v2):');
    for (const line of newContent.split('\n').slice(0, 15)) console.log(`    ${line}`);
    console.log('  ...');
    if (movedFiles.length > 0) {
      console.log(`\n📁 Files to move (${movedFiles.length}):`);
      for (const f of movedFiles) {
        console.log(`  ${path.relative(chainDir, f.from)} → ${path.relative(chainDir, f.to)}`);
      }
    }
    if (createdDirs.length > 0) {
      console.log(`\n📂 Dirs to create (${createdDirs.length}):`);
      for (const d of createdDirs) console.log(`  ${path.relative(chainDir, d)}/`);
    }
    console.log('\n💡 Chạy lại với --write để apply.');
    return result;
  }

  // Write mode — apply changes
  // 1. Backup
  const backupPath = configPath + '.bak';
  fs.copyFileSync(configPath, backupPath);
  result.backupPath = backupPath;

  // 2. Write new config
  fs.writeFileSync(configPath, newContent);

  // 3. Move YAML files
  for (const { from, to } of movedFiles) {
    moveFile(from, to, useGit, chainDir);
  }

  // 4. Create env dirs with .gitkeep
  for (const dir of createdDirs) {
    fs.mkdirSync(dir, { recursive: true });
    const gitkeep = path.join(dir, '.gitkeep');
    if (!fs.existsSync(gitkeep)) {
      fs.writeFileSync(gitkeep, '');
    }
  }

  console.log('✅ Migration complete!');
  console.log(`   Config: schema v1 → v2`);
  console.log(`   Backup: ${path.relative(chainDir, backupPath)}`);
  console.log(`   Files moved: ${movedFiles.length}`);
  console.log(`   Dirs created: ${createdDirs.length}`);
  console.log(`\n   Next: frt-test envs && git status && git add -A && git commit -m "chore: migrate to schema v2"`);

  return result;
}
