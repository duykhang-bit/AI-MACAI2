/**
 * scaffold.ts — Generate chain repo skeleton from chain-template/
 * Supports both legacy (single URL per app) and v2 (multi-env URL per app).
 */

import * as fs from 'fs';
import * as path from 'path';
import { execFileSync } from 'child_process';
import { Command } from 'commander';
import * as yaml from 'js-yaml';

// ============================================================
// Types
// ============================================================
export interface AppInput {
  name: string;
  url?: string;  // legacy: single URL
  urls?: Record<string, string | null>;  // v2: per-env URLs
}

export interface ScaffoldOpts {
  name: string;
  display: string;
  apps: AppInput[];
  envs: string[];
  output: string;
  remote?: string;
  push?: boolean;
}

export interface AddAppOpts {
  name: string;
  url?: string;
  urls?: Record<string, string | null>;
  chainDir?: string;
}

// ============================================================
// Template rendering — simple {{VAR}} substitution
// ============================================================
export function renderTemplate(content: string, vars: Record<string, string>): string {
  return content.replace(/\{\{(\w+)\}\}/g, (_, key) => vars[key] ?? `{{${key}}}`);
}

// ============================================================
// Copy template dir, rendering .tpl files
// ============================================================
export async function copyTemplateDir(
  src: string,
  dst: string,
  vars: Record<string, string>,
): Promise<void> {
  fs.mkdirSync(dst, { recursive: true });

  for (const entry of fs.readdirSync(src, { withFileTypes: true })) {
    const srcPath = path.join(src, entry.name);
    let dstName = entry.name;
    let isTpl = false;

    if (dstName.endsWith('.tpl')) {
      dstName = dstName.slice(0, -4);
      isTpl = true;
    }

    const dstPath = path.join(dst, dstName);

    if (entry.isDirectory()) {
      await copyTemplateDir(srcPath, dstPath, vars);
    } else {
      const content = fs.readFileSync(srcPath, 'utf8');
      fs.writeFileSync(dstPath, isTpl ? renderTemplate(content, vars) : content);
    }
  }
}

// ============================================================
// Git helper
// ============================================================
export function execGit(cwd: string, args: string[]): string {
  return execFileSync('git', args, { cwd, encoding: 'utf8' }).trim();
}

// ============================================================
// Parse multi-URL app string: "name=ci:url,uat:url" or legacy "name=url"
// ============================================================
function parseAppInput(raw: string, envs: string[]): AppInput {
  const [name, ...valueParts] = raw.trim().split('=');
  const value = valueParts.join('=').trim();

  if (!value) {
    return { name: name.trim(), urls: Object.fromEntries(envs.map(e => [e, null])) };
  }

  // Check if it's multi-env format: "ci:url,uat:url"
  if (value.includes(':') && !value.startsWith('http')) {
    const urls: Record<string, string | null> = Object.fromEntries(envs.map(e => [e, null]));
    for (const pair of value.split(',')) {
      const colonIdx = pair.indexOf(':');
      if (colonIdx > 0) {
        const env = pair.slice(0, colonIdx).trim();
        const url = pair.slice(colonIdx + 1).trim();
        urls[env] = url || null;
      }
    }
    return { name: name.trim(), urls };
  }

  // Legacy: single URL → assign to first env, rest null
  const urls: Record<string, string | null> = Object.fromEntries(envs.map(e => [e, null]));
  urls[envs[0]] = value;
  return { name: name.trim(), url: value, urls };
}

// ============================================================
// scaffoldChain
// ============================================================
export async function scaffoldChain(opts: ScaffoldOpts): Promise<void> {
  if (!/^[a-z0-9-]+$/.test(opts.name)) {
    throw new Error(`Chain name must be kebab-case: got "${opts.name}"`);
  }
  if (opts.apps.length === 0) {
    throw new Error('At least 1 app required (--apps "name=url")');
  }
  if (fs.existsSync(opts.output) && fs.readdirSync(opts.output).length > 0) {
    throw new Error(`Output dir not empty: ${opts.output}`);
  }

  const defaultApp = opts.apps[0].name;
  const envs = opts.envs;

  // Build schema v2 config YAML
  const v2Config: any = {
    schema_version: 2,
    environments: Object.fromEntries(envs.map(e => [e, { label: e.toUpperCase() }])),
    apps: {} as any,
    default_app: defaultApp,
    settings: { timeout: 30000, headless: false, screenshotOnFail: true },
  };

  for (const app of opts.apps) {
    v2Config.apps[app.name] = {
      description: `${opts.display} — ${app.name}`,
      urls: app.urls || Object.fromEntries(envs.map(e => [e, null])),
    };
  }

  const configYaml = yaml.dump(v2Config, { lineWidth: -1, noRefs: true, quotingType: '"' });

  // Build template vars
  const appsTable = ['| App | CI URL |', '|-----|--------|']
    .concat(opts.apps.map((a) => {
      const ciUrl = a.urls?.[envs[0]] || a.url || '(not set)';
      return `| ${a.name} | ${ciUrl} |`;
    }))
    .join('\n');

  const vars: Record<string, string> = {
    CHAIN_NAME: opts.display || opts.name.toUpperCase(),
    CHAIN_LOWER: opts.name,
    APPS_YAML: configYaml.trim(),
    APPS_TABLE: appsTable,
    DEFAULT_APP: defaultApp,
  };

  // Copy template
  const templateDir = path.resolve(__dirname, '..', 'chain-template');
  await copyTemplateDir(templateDir, opts.output, vars);

  // Overwrite config with properly formatted v2 YAML (template may render differently)
  const configDir = path.join(opts.output, 'config');
  fs.mkdirSync(configDir, { recursive: true });
  fs.writeFileSync(path.join(configDir, 'environments.yaml'), configYaml);

  // Create per-app, per-env flow folders
  for (const app of opts.apps) {
    fs.mkdirSync(path.join(opts.output, 'flows', app.name, '_shared'), { recursive: true });
    for (const env of envs) {
      const envDir = path.join(opts.output, 'flows', app.name, env);
      fs.mkdirSync(envDir, { recursive: true });
      fs.writeFileSync(path.join(envDir, '.gitkeep'), '');
    }
  }

  // Git init
  execGit(opts.output, ['init', '-b', 'main']);
  execGit(opts.output, ['add', '.']);
  execGit(opts.output, ['commit', '-m', `init: scaffold ${opts.name} chain`]);

  if (opts.remote) {
    execGit(opts.output, ['remote', 'add', 'origin', opts.remote]);
  }

  if (opts.push && opts.remote) {
    execGit(opts.output, ['push', '-u', 'origin', 'main']);
  }

  console.log(`\n✅ Chain "${opts.name}" scaffolded at: ${opts.output}`);
  console.log(`   Apps: ${opts.apps.map((a) => a.name).join(', ')}`);
  console.log(`   Envs: ${envs.join(', ')}`);
  console.log(`   Default app: ${defaultApp}`);
  if (opts.remote) console.log(`   Remote: ${opts.remote}`);
  console.log(`\n   Next: cd ${opts.output} && frt-test envs`);
}

// ============================================================
// addAppToChain
// ============================================================
export async function addAppToChain(opts: AddAppOpts): Promise<void> {
  const chainDir = opts.chainDir || process.cwd();
  const configPath = path.join(chainDir, 'config', 'environments.yaml');

  if (!fs.existsSync(configPath)) {
    throw new Error(`Not a chain repo (config/environments.yaml not found in ${chainDir})`);
  }

  const raw = yaml.load(fs.readFileSync(configPath, 'utf8')) as any;

  if (raw.apps?.[opts.name]) {
    throw new Error(`App "${opts.name}" already exists in config/environments.yaml`);
  }

  // Determine if v2 or v1
  const isV2 = raw.schema_version === 2 || Object.values(raw.apps || {}).some((a: any) => a?.urls);

  if (isV2) {
    const envs = raw.environments ? Object.keys(raw.environments) : ['ci', 'uat', 'prod'];
    const urls = opts.urls || Object.fromEntries(envs.map(e => [e, null]));
    // If legacy --url provided, assign to first env
    if (opts.url && !opts.urls) {
      urls[envs[0]] = opts.url;
    }
    if (!raw.apps) raw.apps = {};
    raw.apps[opts.name] = { description: opts.name, urls };
    fs.writeFileSync(configPath, yaml.dump(raw, { lineWidth: -1, noRefs: true, quotingType: '"' }));

    // Create flow folders
    for (const env of envs) {
      const envDir = path.join(chainDir, 'flows', opts.name, env);
      fs.mkdirSync(envDir, { recursive: true });
      fs.writeFileSync(path.join(envDir, '.gitkeep'), '');
    }
    fs.mkdirSync(path.join(chainDir, 'flows', opts.name, '_shared'), { recursive: true });
  } else {
    // Legacy v1 add
    const content = fs.readFileSync(configPath, 'utf8');
    if (content.includes(`  ${opts.name}:`)) {
      throw new Error(`App "${opts.name}" already exists in config/environments.yaml`);
    }
    const newEntry = `  ${opts.name}:\n    baseUrl: "${opts.url || ''}"\n    description: "${opts.name}"`;
    const updated = content.replace(/^(default_app:)/m, `${newEntry}\n\n$1`);
    fs.writeFileSync(configPath, updated);
    fs.mkdirSync(path.join(chainDir, 'flows', opts.name, '_shared'), { recursive: true });
  }

  console.log(`\n✅ App "${opts.name}" added.`);
  if (opts.url) console.log(`   URL: ${opts.url}`);
  console.log(`   Flows dir: flows/${opts.name}/`);
  console.log(`\n   Next: git add . && git commit -m "add: app ${opts.name}" && git push`);
}

// ============================================================
// Register CLI commands (called from cli.ts)
// ============================================================
export function registerScaffoldCommands(program: Command): void {
  program
    .command('scaffold-chain')
    .description('Tạo chain repo mới từ template (onboard chuỗi)')
    .requiredOption('--name <name>', 'Chain name (kebab-case, vd: lab, vac, ict)')
    .option('--display <display>', 'Display name (vd: "LAB (Xét nghiệm)")')
    .requiredOption('--apps <apps...>', 'Apps: "name=env:url,env:url" or "name=url"')
    .requiredOption('--output <path>', 'Output directory')
    .option('--envs <envs>', 'Environments comma-separated (default: ci,uat,prod)', 'ci,uat,prod')
    .option('--remote <url>', 'Git remote URL')
    .option('--push', 'Push to remote after scaffold')
    .action(async (opts) => {
      const envs = opts.envs.split(',').map((e: string) => e.trim());
      const apps = (opts.apps as string[]).map((raw: string) => parseAppInput(raw, envs));
      await scaffoldChain({
        name: opts.name,
        display: opts.display || opts.name.toUpperCase(),
        apps,
        envs,
        output: path.resolve(opts.output),
        remote: opts.remote,
        push: opts.push || false,
      });
    });

  program
    .command('add-app')
    .description('Thêm app mới vào chuỗi hiện tại')
    .requiredOption('--name <name>', 'App name (kebab-case)')
    .option('--url <url>', 'App base URL (legacy, assigns to first env)')
    .option('--urls <urls>', 'Multi-env URLs: "ci:url,uat:url"')
    .action(async (opts) => {
      let urls: Record<string, string | null> | undefined;
      if (opts.urls) {
        urls = {};
        for (const pair of opts.urls.split(',')) {
          const colonIdx = pair.indexOf(':');
          if (colonIdx > 0) {
            urls[pair.slice(0, colonIdx).trim()] = pair.slice(colonIdx + 1).trim() || null;
          }
        }
      }
      await addAppToChain({ name: opts.name, url: opts.url, urls });
    });
}
